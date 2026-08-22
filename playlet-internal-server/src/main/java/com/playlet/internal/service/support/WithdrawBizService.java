package com.playlet.internal.service.support;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.WithdrawHomeRespEntity;
import com.playlet.internal.api.response.WithdrawHomeRespEntity.WithdrawAssetItemEntity;
import com.playlet.internal.api.response.WithdrawRecordItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.constants.WithdrawConstants;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.dao.welfare.WithdrawConfigDao;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.entity.welfare.WithdrawConfigEntity;
import com.playlet.internal.enums.OnePayBindStatusEnums;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
	private WithdrawPayoutService withdrawPayoutService;

	/** 提现首页：可用余额、绑定状态、今日已用、资产配置；uid 为空时金币等为 0 */
	public ResponseBase home(Integer uid, WithdrawUserTypeEnums userType) {
		WithdrawHomeRespEntity resp = new WithdrawHomeRespEntity();
		if (uid != null) {
			WithdrawWalletHandler handler = walletHandlerRegistry.of(userType.getCode());
			WithdrawWalletSnapshot snap = handler.load(uid);
			// 可提 = 总余额 - 冻结
			resp.setCoinBalance(snap.getCoinBalance() - snap.getFrozenCoinBalance());
			resp.setFrozenCoinBalance(snap.getFrozenCoinBalance());
			resp.setOnepayBindStatus(nvlInt(snap.getOnepayBindStatus()));
			Integer allUsed = userWithdrawOrderDao.sumPointsToday(uid, userType.getCode(), todayStart());
			resp.setTodayUsedPoints(allUsed == null ? 0 : allUsed);
			UserWithdrawOrderEntity latestAny = userWithdrawOrderDao.findLatestByUid(uid, userType.getCode());
			if (latestAny != null && !StringUtils.isEmpty(latestAny.getOnepayAccount())) {
				resp.setLastWalletAddress(latestAny.getOnepayAccount());
			}
		} else {
			// 未登录：仅展示资产配置，用户金币/绑定/今日已用均为 0
			resp.setCoinBalance(0L);
			resp.setFrozenCoinBalance(0L);
			resp.setOnepayBindStatus(0);
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
				UserWithdrawOrderEntity latest = userWithdrawOrderDao.findLatestByUidAndAsset(uid, userType.getCode(),
						cfg.getAssetCode(), cfg.getNetwork());
				if (latest != null && !StringUtils.isEmpty(latest.getOnepayAccount())) {
					item.setLastWalletAddress(latest.getOnepayAccount());
				}
			} else {
				item.setTodayUsedPoints(0);
			}
			assets.add(item);
		}
		resp.setAssets(assets);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	/** 提交提现：只冻结不扣减，事务提交后再通知 OnePay */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase submit(Integer uid, Integer pointsRaw, WithdrawUserTypeEnums userType) {
		int points = pointsRaw == null ? 0 : pointsRaw;
		if (points <= 0) {
			return setResultError(I18nUtil.getMessage("withdraw.points_invalid"));
		}
		WithdrawWalletHandler handler = walletHandlerRegistry.of(userType.getCode());
		WithdrawWalletSnapshot snap = handler.load(uid);
		if (!Integer.valueOf(OnePayBindStatusEnums.BOUND.getCode()).equals(snap.getOnepayBindStatus())
				|| StringUtils.isEmpty(snap.getOnepayAccount())) {
			return setResultError(I18nUtil.getMessage("withdraw.onepay_not_bound"));
		}
		long available = snap.getCoinBalance() - snap.getFrozenCoinBalance();
		if (available < points) {
			return setResultError(I18nUtil.getMessage("withdraw.balance_not_enough"));
		}
		WithdrawConfigEntity cfg = withdrawConfigDao.findActive(
				WithdrawConstants.ASSET_ONEPAY, WithdrawConstants.NETWORK_ONEPAY);
		if (cfg != null) {
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
			UserWithdrawOrderEntity order = buildOrder(uid, userType, snap.getOnepayAccount(),
					handler.findOpenId(uid), points, cfg, orderNo);
			GenericityUtil.setDate(order);
			userWithdrawOrderDao.insert(order);
			// 提交提现先记冻结流水，成功/失败再分别记扣减或退回
			handler.writeWithdrawFreezeLedger(uid, points, orderNo);
			final Long orderId = order.getId();
			// 避免外部已受理、本地事务却回滚
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						withdrawPayoutService.payoutAsync(orderId);
					}
				});
			} else {
				withdrawPayoutService.payoutAsync(orderId);
			}
			log.info("withdraw submit userType={} uid={} orderNo={} points={}",
					userType.getCode(), uid, orderNo, points);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("withdraw submit failed userType={} uid={} orderNo={}",
					userType.getCode(), uid, orderNo, e);
			TransactionUtils.markRollbackOnly();
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	/** 提现记录：按主体隔离分页，地址脱敏 */
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

	/** 落单快照：userType 必须写入，回调靠它路由钱包 */
	private UserWithdrawOrderEntity buildOrder(Integer uid, WithdrawUserTypeEnums userType, String onepayAccount,
			String onepayOpenId, int points, WithdrawConfigEntity cfg, String orderNo) {
		UserWithdrawOrderEntity order = new UserWithdrawOrderEntity();
		order.setOrderNo(orderNo);
		order.setUid(uid);
		order.setUserType(userType.getCode());
		order.setAssetCode(cfg == null ? WithdrawConstants.ASSET_ONEPAY : cfg.getAssetCode());
		order.setNetwork(cfg == null ? WithdrawConstants.NETWORK_ONEPAY : cfg.getNetwork());
		order.setOnepayAccount(onepayAccount);
		order.setOnepayOpenId(onepayOpenId);
		order.setPointsAmt(points);
		order.setRate(cfg == null || cfg.getPointsPerUnit() == null ? 1 : cfg.getPointsPerUnit());
		order.setFeeAmt(cfg == null ? BigDecimal.ZERO : scale(cfg.getServiceFee()));
		order.setGrossAmt(BigDecimal.valueOf(points));
		order.setActualAmt(BigDecimal.valueOf(points));
		order.setStatus(WithdrawOrderStatusEnums.PENDING.getCode());
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
		item.setOnepayAccountMasked(maskAccount(row.getOnepayAccount()));
		item.setStatus(row.getStatus());
		item.setStatusLabel(WithdrawOrderStatusEnums.getLableByCode(row.getStatus()));
		item.setThirdOrderNo(row.getThirdOrderNo());
		item.setFailReason(row.getFailReason());
		item.setSetTime(row.getSetTime());
		return item;
	}

	/** C 端 W、作家 CW，降低单号撞车概率 */
	private static String orderPrefix(WithdrawUserTypeEnums userType) {
		if (userType == WithdrawUserTypeEnums.CREATOR) {
			return WithdrawConstants.ORDER_NO_PREFIX_CREATOR;
		}
		return WithdrawConstants.ORDER_NO_PREFIX_APP;
	}

	/** OnePay 账号脱敏：邮箱走名称脱敏，其他走前2后2 */
	private static String maskAccount(String account) {
		if (StringUtils.isEmpty(account)) {
			return account;
		}
		if (account.contains("@")) {
			int at = account.indexOf('@');
			String name = account.substring(0, at);
			String domain = account.substring(at);
			if (name.length() <= 1) {
				return "*" + domain;
			}
			return name.charAt(0) + "***" + domain;
		}
		if (account.length() < 6) {
			return "***";
		}
		return account.substring(0, 2) + "***" + account.substring(account.length() - 2);
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

	private static int nvlInt(Integer v) {
		return v == null ? 0 : v;
	}
}
