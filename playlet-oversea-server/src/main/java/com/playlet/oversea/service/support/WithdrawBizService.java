package com.playlet.oversea.service.support;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.api.request.WithdrawReqEntity;
import com.playlet.oversea.api.response.WithdrawHomeRespEntity;
import com.playlet.oversea.api.response.WithdrawHomeRespEntity.WithdrawAssetItemEntity;
import com.playlet.oversea.api.response.WithdrawRecordItemEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.RedisKeyConstants;
import com.playlet.oversea.constants.WithdrawConstants;
import com.playlet.oversea.dao.wallet.WalletAccountDao;
import com.playlet.oversea.dao.welfare.UserWithdrawOrderDao;
import com.playlet.oversea.dao.welfare.WithdrawConfigDao;
import com.playlet.oversea.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.oversea.entity.welfare.WithdrawConfigEntity;
import com.playlet.oversea.enums.WithdrawOrderStatusEnums;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import com.playlet.oversea.service.support.WithdrawAmountCalculator.Result;
import com.playlet.oversea.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * C 端 / 作家端共用提现：首页、提交、记录。账户差异由 WalletHandler 消化。
 */
@Slf4j
@Service
public class WithdrawBizService extends BaseApiService {

	/** 日限额按上海自然日切 */
	private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
	private static final DateTimeFormatter DAY_START_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	/** 金额展示位数，截断不四舍五入 */
	private static final int AMT_SCALE = 8;

	@Autowired
	private WithdrawWalletHandlerRegistry walletHandlerRegistry;
	@Autowired
	private WithdrawConfigDao withdrawConfigDao;
	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private WithdrawWalletAccountSupport withdrawWalletAccountSupport;
	@Autowired
	private WithdrawWalletSupport withdrawWalletSupport;
    @Autowired
    private WalletAccountDao walletAccountDao;

	/** 提现首页：可用余额、钱包就绪、今日已用、资产配置；uid 为空时金币等为 0 */
	public ResponseBase home(Integer uid, WithdrawUserTypeEnums userType) {
		WithdrawHomeRespEntity resp = new WithdrawHomeRespEntity();
		if (uid != null) {
			WithdrawWalletHandler handler = walletHandlerRegistry.of(userType.getCode());
			WithdrawWalletSnapshot snap = handler.load(uid);
			boolean walletReady = withdrawWalletAccountSupport.isReady(userType.getCode(), uid);
			snap.setWalletWithdrawReady(walletReady ? 1 : 0);
			// 可提 = 总余额 - 冻结
			resp.setCoinBalance(snap.getCoinBalance() - snap.getFrozenCoinBalance());
			resp.setFrozenCoinBalance(snap.getFrozenCoinBalance());
			resp.setWalletWithdrawReady(walletReady ? 1 : 0);
			Integer allUsed = userWithdrawOrderDao.sumPointsToday(uid, userType.getCode(), todayStart());
			resp.setTodayUsedPoints(allUsed == null ? 0 : allUsed);
			resp.setLastWalletAddress(WithdrawConstants.PAYOUT_TARGET_BALANCE_LABEL);
		} else {
			// 未登录：仅展示资产配置，用户金币/绑定/今日已用均为 0
			resp.setCoinBalance(0L);
			resp.setFrozenCoinBalance(0L);
			resp.setWalletWithdrawReady(0);
			resp.setTodayUsedPoints(0);
		}

		List<WithdrawConfigEntity> cfgs = withdrawConfigDao.findActiveList();
		if (cfgs == null || cfgs.isEmpty()) {
			resp.setWithdrawEnabled(false);
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		}
		resp.setWithdrawEnabled(true);
		List<WithdrawAssetItemEntity> assets = new ArrayList<>();
		for (WithdrawConfigEntity cfg : cfgs) {
			WithdrawAssetItemEntity item = new WithdrawAssetItemEntity();
			item.setAssetCode(cfg.getAssetCode());
			item.setNetwork(cfg.getNetwork());
			item.setPointsPerUnit(cfg.getPointsPerUnit());
			item.setServiceFee(scale(cfg.getServiceFee()));
			item.setMinWithdrawPoints(cfg.getMinWithdrawPoints());
			item.setMaxWithdrawPointsDay(cfg.getMaxWithdrawPointsDay() == null ? 0 : cfg.getMaxWithdrawPointsDay());
			if (uid != null) {
				Integer used = userWithdrawOrderDao.sumPointsTodayByAsset(uid, userType.getCode(), cfg.getAssetCode(),
						cfg.getNetwork(), todayStart());
				item.setTodayUsedPoints(used == null ? 0 : used);
				item.setLastWalletAddress(WithdrawConstants.PAYOUT_TARGET_BALANCE_LABEL);
			} else {
				item.setTodayUsedPoints(0);
			}
			assets.add(item);
		}
		resp.setAssets(assets);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/** 提交提现：按配置换算为 U，同步扣减金币并入账 wallet_account.available_balance */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase submit(Integer uid, WithdrawReqEntity req, WithdrawUserTypeEnums userType) {
		int points = req == null || req.getPoints() == null ? 0 : req.getPoints();
		if (points <= 0) {
			return setResultError(I18nUtil.getMessage("withdraw.points_invalid"));
		}
		// 支付密码校验
		String payPassword = walletAccountDao.selectPayPasswordById(uid);
		if (!PasswordHashUtils.matches(req.getPayPassword(), payPassword)) {
			return setResultError(I18nUtil.getMessage("pay_password_error"));
		}
		if (!withdrawWalletAccountSupport.isReady(userType.getCode(), uid)) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		WithdrawConfigEntity cfg = resolveConfig(req);
		if (cfg == null) {
			return setResultError(I18nUtil.getMessage("withdraw.disabled"));
		}
		Result amount = WithdrawAmountCalculator.calculate(points, cfg);
		if (amount.getActualAmt().signum() <= 0) {
			return setResultError(I18nUtil.getMessage("withdraw.actual_zero"));
		}
		WithdrawWalletHandler handler = walletHandlerRegistry.of(userType.getCode());
		WithdrawWalletSnapshot snap = handler.load(uid);
		long available = snap.getCoinBalance() - snap.getFrozenCoinBalance();
		if (available < points) {
			return setResultError(I18nUtil.getMessage("withdraw.balance_not_enough"));
		}
		int min = cfg.getMinWithdrawPoints() == null ? 0 : cfg.getMinWithdrawPoints();
		if (points < min) {
			return setResultError(I18nUtil.getMessage("withdraw.below_min", String.valueOf(min)));
		}
		int dayMax = cfg.getMaxWithdrawPointsDay() == null ? 0 : cfg.getMaxWithdrawPointsDay();
		if (dayMax > 0) {
			Integer used = userWithdrawOrderDao.sumPointsTodayByAsset(uid, userType.getCode(), cfg.getAssetCode(),
					cfg.getNetwork(), todayStart());
			int usedAmt = used == null ? 0 : used;
			if (usedAmt + points > dayMax) {
				return setResultError(I18nUtil.getMessage("withdraw.day_limit"));
			}
		}

		// 锁带 userType，避免 C 端与作家同数字 ID 互相挡住
		String lockKey = RedisKeyConstants.WITHDRAW_SUBMIT_LOCK + userType.getCode() + ":" + uid;
		if (redisUtil.hasKey(lockKey)) {
			return setResultError(I18nUtil.getMessage("withdraw.too_frequent"));
		}
		redisUtil.set(lockKey, "1", RedisKeyConstants.WITHDRAW_SUBMIT_LOCK_SEC);

		String orderNo = orderPrefix(userType) + OrderCodeFactory.getOrderCode(uid.longValue());
		try {
			if (handler.freeze(uid, points) <= 0) {
				return setResultError(I18nUtil.getMessage("withdraw.balance_not_enough"));
			}
			UserWithdrawOrderEntity order = buildOrder(uid, userType, points, cfg, amount, orderNo);
			GenericityUtil.setDate(order);
			userWithdrawOrderDao.insert(order);
			handler.writeWithdrawFreezeLedger(uid, points, orderNo);
			// 入账钱包余额并写 wallet_log（非卡交易）
			withdrawWalletAccountSupport.creditCoinWithdraw(
					userType.getCode(), uid, amount.getActualAmt(), amount.getFeeAmt(), points, orderNo, order.getId());
			if (handler.settleFrozen(uid, points) <= 0) {
				throw new BaseException("settle frozen coin failed");
			}
			// 金币扣减流水须在 settle 之后写入，余额快照才与库一致
			handler.writeWithdrawLedger(uid, points, orderNo);
			log.info("withdraw success userType={} uid={} orderNo={} points={} actualU={}",
					userType.getCode(), uid, orderNo, points, amount.getActualAmt());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("withdraw submit biz failed userType={} uid={} orderNo={}",
					userType.getCode(), uid, orderNo, e);
			TransactionUtils.markRollbackOnly();
			throw e;
		} catch (Exception e) {
			log.error("withdraw submit failed userType={} uid={} orderNo={}",
					userType.getCode(), uid, orderNo, e);
			TransactionUtils.markRollbackOnly();
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 提现记录：按主体隔离分页 */
	public ResponseBase records(PageQueryHelperEntity page, Integer uid, WithdrawUserTypeEnums userType) {
		if (page == null) {
			page = new PageQueryHelperEntity();
		}
		PageHelper.startPage(page.getPageNumber(), page.getPageSize());
		List<UserWithdrawOrderEntity> rows = userWithdrawOrderDao.findByUid(uid, userType.getCode());
		if (rows == null) {
			rows = new ArrayList<>();
		}
		PageInfo<UserWithdrawOrderEntity> basePage = new PageInfo<>(rows);
		List<WithdrawRecordItemEntity> items = new ArrayList<>();
		for (UserWithdrawOrderEntity row : rows) {
			items.add(toRecordItem(row));
		}
		PageInfo<WithdrawRecordItemEntity> pageInfo = new PageInfo<>(items);
		pageInfo.setTotal(basePage.getTotal());
		pageInfo.setPages(basePage.getPages());
		pageInfo.setPageNum(basePage.getPageNum());
		pageInfo.setPageSize(basePage.getPageSize());
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	private WithdrawConfigEntity resolveConfig(WithdrawReqEntity req) {
		String assetCode = req == null ? null : req.getAssetCode();
		String network = req == null ? null : req.getNetwork();
		if (!StringUtils.isEmpty(assetCode) && !StringUtils.isEmpty(network)) {
			WithdrawConfigEntity cfg = withdrawConfigDao.findActive(assetCode.trim(), network.trim());
			if (cfg == null) {
				return null;
			}
			return cfg;
		}
		List<WithdrawConfigEntity> cfgs = withdrawConfigDao.findActiveList();
		if (cfgs == null || cfgs.isEmpty()) {
			return null;
		}
		return cfgs.get(0);
	}

	/** 落单快照：同步成功，gateway=BALANCE */
	private UserWithdrawOrderEntity buildOrder(Integer uid, WithdrawUserTypeEnums userType, int points,
			WithdrawConfigEntity cfg, Result amount, String orderNo) {
		UserWithdrawOrderEntity order = new UserWithdrawOrderEntity();
		order.setOrderNo(orderNo);
		order.setUid(uid);
		order.setUserType(userType.getCode());
		order.setAssetCode(cfg.getAssetCode());
		order.setNetwork(cfg.getNetwork());
		order.setGateway(WithdrawConstants.GATEWAY_BALANCE);
		order.setRequestOrderId(orderNo);
		order.setPointsAmt(points);
		order.setRate(amount.getPointsPerUnit());
		order.setFeeAmt(amount.getFeeAmt());
		order.setGrossAmt(amount.getGrossAmt());
		order.setActualAmt(amount.getActualAmt());
		order.setStatus(WithdrawOrderStatusEnums.SUCCESS.getCode());
		return order;
	}

	private WithdrawRecordItemEntity toRecordItem(UserWithdrawOrderEntity row) {
		WithdrawRecordItemEntity item = new WithdrawRecordItemEntity();
		item.setId(row.getId());
		item.setOrderNo(row.getOrderNo());
		item.setAssetCode(row.getAssetCode());
		item.setNetwork(row.getNetwork());
		// 列表展示为支出
		item.setPointsAmt(row.getPointsAmt() == null ? 0 : -Math.abs(row.getPointsAmt()));
		item.setActualAmt(scale(row.getActualAmt()));
		item.setPayoutTargetMasked(resolvePayoutTarget(row));
		item.setStatus(row.getStatus());
		item.setStatusLabel(WithdrawOrderStatusEnums.getLableByCode(row.getStatus()));
		item.setThirdOrderNo(row.getThirdOrderNo());
		item.setFailReason(row.getFailReason());
		item.setSetTime(row.getSetTime());
		return item;
	}

	private String resolvePayoutTarget(UserWithdrawOrderEntity row) {
		if (row != null && WithdrawConstants.GATEWAY_BALANCE.equalsIgnoreCase(row.getGateway())) {
			return WithdrawConstants.PAYOUT_TARGET_BALANCE_LABEL;
		}
		return withdrawWalletSupport.maskTargetByBankcardId(
				row == null ? null : row.getTargetBankcardId());
	}

	/** C 端 W、作家 CW，降低单号撞车概率 */
	private static String orderPrefix(WithdrawUserTypeEnums userType) {
		if (userType == WithdrawUserTypeEnums.CREATOR) {
			return WithdrawConstants.ORDER_NO_PREFIX_CREATOR;
		}
		return WithdrawConstants.ORDER_NO_PREFIX_APP;
	}

	private static BigDecimal scale(BigDecimal v) {
		if (v == null) {
			return BigDecimal.ZERO.setScale(AMT_SCALE, RoundingMode.DOWN);
		}
		return v.setScale(AMT_SCALE, RoundingMode.DOWN);
	}

	/** 当日 00:00:00，供日限额累计 */
	private static String todayStart() {
		LocalDateTime start = LocalDate.now(ZONE).atStartOfDay();
		return start.format(DAY_START_FMT);
	}
}
