package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.WithdrawHomeRespEntity;
import com.playlet.internal.api.response.WithdrawHomeRespEntity.WithdrawAssetItemEntity;
import com.playlet.internal.api.response.WithdrawPreviewRespEntity;
import com.playlet.internal.api.response.WithdrawRecordItemEntity;
import com.playlet.internal.api.response.WithdrawSubmitRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.dao.welfare.WithdrawConfigDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.entity.welfare.WithdrawConfigEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WithdrawNetworkEnums;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.query.welfare.WithdrawPreviewQuery;
import com.playlet.internal.query.welfare.WithdrawSubmitQuery;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.service.WithdrawService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.RedisUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class WithdrawServiceImpl extends BaseApiService implements WithdrawService {

	/** TRC20 / Tron Base58Check */
	private static final Pattern TRC20_ADDRESS = Pattern.compile("^T[1-9A-HJ-NP-Za-km-z]{33}$");
	/** ERC20 / Ethereum hex */
	private static final Pattern ERC20_ADDRESS = Pattern.compile("^0x[0-9a-fA-F]{40}$");
	private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
	private static final DateTimeFormatter DAY_START_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final int AMT_SCALE = 8;

	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private WithdrawConfigDao withdrawConfigDao;
	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private RedisUtil redisUtil;
	@Autowired
	private WithdrawPayoutService withdrawPayoutService;

	@Override
	public ResponseBase withdrawHome(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		WithdrawHomeRespEntity resp = new WithdrawHomeRespEntity();
		resp.setCoinBalance(account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance());
		Integer allUsed = userWithdrawOrderDao.sumPointsToday(uid, todayStart());
		resp.setTodayUsedPoints(allUsed == null ? 0 : allUsed);
		UserWithdrawOrderEntity latestAny = userWithdrawOrderDao.findLatestByUid(uid);
		if (latestAny != null && !StringUtils.isEmpty(latestAny.getWalletAddress())) {
			resp.setLastWalletAddress(latestAny.getWalletAddress());
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
			Integer used = userWithdrawOrderDao.sumPointsTodayByAsset(uid, cfg.getAssetCode(),
					cfg.getNetwork(), todayStart());
			item.setTodayUsedPoints(used == null ? 0 : used);
			UserWithdrawOrderEntity latest = userWithdrawOrderDao.findLatestByUidAndAsset(uid,
					cfg.getAssetCode(), cfg.getNetwork());
			if (latest != null && !StringUtils.isEmpty(latest.getWalletAddress())) {
				item.setLastWalletAddress(latest.getWalletAddress());
			}
			assets.add(item);
		}
		resp.setAssets(assets);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase withdrawPreview(@Valid @RequestBody WithdrawPreviewQuery query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		WithdrawConfigEntity cfg = requireActiveConfig(query.getAssetCode(), query.getNetwork());
		if (cfg == null) {
			return setResultError(I18nUtil.getMessage("withdraw.asset_invalid"));
		}
		int points = query.getPoints() == null ? 0 : query.getPoints();
		if (points <= 0) {
			return setResultError(I18nUtil.getMessage("withdraw.points_invalid"));
		}
		return setResultSuccess(calcPreview(cfg, points), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase withdrawSubmit(@Valid @RequestBody WithdrawSubmitQuery query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		WithdrawConfigEntity cfg = requireActiveConfig(query.getAssetCode(), query.getNetwork());
		if (cfg == null) {
			return setResultError(I18nUtil.getMessage("withdraw.asset_invalid"));
		}
		String address = query.getWalletAddress() == null ? "" : query.getWalletAddress().trim();
		if (!isValidAddress(cfg.getNetwork(), address)) {
			return setResultError(I18nUtil.getMessage("withdraw.address_invalid"));
		}
		int points = query.getPoints() == null ? 0 : query.getPoints();
		if (points <= 0) {
			return setResultError(I18nUtil.getMessage("withdraw.points_invalid"));
		}
		int min = cfg.getMinWithdrawPoints() == null ? 0 : cfg.getMinWithdrawPoints();
		if (points < min) {
			return setResultError(I18nUtil.getMessage("withdraw.below_min", String.valueOf(min)));
		}
		int dayMax = cfg.getMaxWithdrawPointsDay() == null ? 0 : cfg.getMaxWithdrawPointsDay();
		if (dayMax > 0) {
			Integer used = userWithdrawOrderDao.sumPointsTodayByAsset(uid, cfg.getAssetCode(),
					cfg.getNetwork(), todayStart());
			int usedAmt = used == null ? 0 : used;
			if (usedAmt + points > dayMax) {
				return setResultError(I18nUtil.getMessage("withdraw.day_limit"));
			}
		}
		WithdrawPreviewRespEntity preview = calcPreview(cfg, points);
		if (preview.getActualAmt() == null || preview.getActualAmt().compareTo(BigDecimal.ZERO) <= 0) {
			return setResultError(I18nUtil.getMessage("withdraw.actual_zero"));
		}

		String lockKey = RedisKeyConstants.WITHDRAW_SUBMIT_LOCK + uid;
		if (redisUtil.hasKey(lockKey)) {
			return setResultError(I18nUtil.getMessage("withdraw.too_frequent"));
		}
		redisUtil.set(lockKey, "1", RedisKeyConstants.WITHDRAW_SUBMIT_LOCK_SEC);

		String orderNo = "W" + OrderCodeFactory.getOrderCode(uid.longValue());
		boolean deducted;
		try {
			deducted = deductCoin(uid, points, CoinBizTypeEnums.WITHDRAW.getName(),
					"WITHDRAW:" + orderNo, "", "提现扣减");
		} catch (Exception e) {
			log.warn("withdraw deduct failed uid={} orderNo={}: {}", uid, orderNo, e.getMessage());
			TransactionUtils.markRollbackOnly();
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!deducted) {
			return setResultError(I18nUtil.getMessage("withdraw.balance_not_enough"));
		}

		UserWithdrawOrderEntity order = new UserWithdrawOrderEntity();
		order.setOrderNo(orderNo);
		order.setUid(uid);
		order.setAssetCode(cfg.getAssetCode());
		order.setNetwork(cfg.getNetwork());
		order.setWalletAddress(address);
		order.setPointsAmt(points);
		order.setRate(cfg.getPointsPerUnit());
		order.setFeeAmt(preview.getFeeAmt());
		order.setGrossAmt(preview.getGrossAmt());
		order.setActualAmt(preview.getActualAmt());
		order.setStatus(WithdrawOrderStatusEnums.PENDING.getCode());
		try {
			GenericityUtil.setDate(order);
			userWithdrawOrderDao.insert(order);
		} catch (Exception e) {
			log.error("insert withdraw order failed orderNo={}", orderNo, e);
			// 依赖事务回滚扣款，不再手动 creditCoin 补偿
			TransactionUtils.markRollbackOnly();
			return setResultError(I18nUtil.getMessage("base_error"));
		}

		final Long orderId = order.getId();
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

		AppAccountEntity account = appAccountDao.findByUid(uid);
		WithdrawSubmitRespEntity resp = new WithdrawSubmitRespEntity();
		resp.setOrderNo(orderNo);
		resp.setAssetCode(cfg.getAssetCode());
		resp.setNetwork(cfg.getNetwork());
		resp.setStatus(order.getStatus());
		resp.setPointsAmt(points);
		resp.setActualAmt(preview.getActualAmt().stripTrailingZeros().toPlainString());
		resp.setCoinBalance(account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance());
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (page == null) {
			page = new PageQueryHelperEntity();
		}
		PageHelper.startPage(page.getPageNumber(), page.getPageSize());
		List<UserWithdrawOrderEntity> rows = userWithdrawOrderDao.findByUid(uid);
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

	private WithdrawConfigEntity requireActiveConfig(String assetCode, String network) {
		if (StringUtils.isEmpty(assetCode) || StringUtils.isEmpty(network)) {
			return null;
		}
		String code = assetCode.trim().toUpperCase();
		String net = network.trim().toUpperCase();
		if (WithdrawNetworkEnums.fromCode(net) == null) {
			return null;
		}
		WithdrawConfigEntity cfg = withdrawConfigDao.findActive(code, net);
		if (cfg == null || !Integer.valueOf(1).equals(cfg.getStatus())) {
			return null;
		}
		if (cfg.getPointsPerUnit() == null || cfg.getPointsPerUnit() <= 0) {
			return null;
		}
		return cfg;
	}

	private WithdrawPreviewRespEntity calcPreview(WithdrawConfigEntity cfg, int points) {
		BigDecimal rate = BigDecimal.valueOf(cfg.getPointsPerUnit());
		BigDecimal gross = BigDecimal.valueOf(points).divide(rate, AMT_SCALE, RoundingMode.DOWN);
		BigDecimal fee = scale(cfg.getServiceFee() == null ? BigDecimal.ZERO : cfg.getServiceFee());
		BigDecimal actual = gross.subtract(fee);
		if (actual.compareTo(BigDecimal.ZERO) < 0) {
			actual = BigDecimal.ZERO.setScale(AMT_SCALE, RoundingMode.DOWN);
		} else {
			actual = actual.setScale(AMT_SCALE, RoundingMode.DOWN);
		}
		WithdrawPreviewRespEntity resp = new WithdrawPreviewRespEntity();
		resp.setAssetCode(cfg.getAssetCode());
		resp.setNetwork(cfg.getNetwork());
		resp.setPoints(points);
		resp.setPointsPerUnit(cfg.getPointsPerUnit());
		resp.setGrossAmt(gross);
		resp.setFeeAmt(fee);
		resp.setActualAmt(actual);
		return resp;
	}

	private WithdrawRecordItemEntity toRecordItem(UserWithdrawOrderEntity row) {
		WithdrawRecordItemEntity item = new WithdrawRecordItemEntity();
		item.setId(row.getId());
		item.setOrderNo(row.getOrderNo());
		item.setAssetCode(row.getAssetCode());
		item.setNetwork(row.getNetwork());
		item.setPointsAmt(row.getPointsAmt() == null ? 0 : -Math.abs(row.getPointsAmt()));
		item.setActualAmt(scale(row.getActualAmt()));
		item.setWalletAddressMasked(maskAddress(row.getWalletAddress()));
		item.setStatus(row.getStatus());
		item.setStatusLabel(WithdrawOrderStatusEnums.getLableByCode(row.getStatus()));
		item.setTxHash(row.getTxHash());
		item.setFailReason(row.getFailReason());
		item.setSetTime(row.getSetTime());
		return item;
	}

	private static boolean isValidAddress(String network, String address) {
		if (StringUtils.isEmpty(address)) {
			return false;
		}
		WithdrawNetworkEnums net = WithdrawNetworkEnums.fromCode(network);
		if (net == null) {
			return false;
		}
		if (net == WithdrawNetworkEnums.TRC20) {
			return TRC20_ADDRESS.matcher(address).matches();
		}
		if (net == WithdrawNetworkEnums.ERC20) {
			return ERC20_ADDRESS.matcher(address).matches();
		}
		return false;
	}

	private static String maskAddress(String address) {
		if (StringUtils.isEmpty(address) || address.length() < 12) {
			return address;
		}
		return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
	}

	private static BigDecimal scale(BigDecimal v) {
		if (v == null) {
			return BigDecimal.ZERO.setScale(AMT_SCALE, RoundingMode.DOWN);
		}
		return v.setScale(AMT_SCALE, RoundingMode.DOWN);
	}

	private static String todayStart() {
		LocalDateTime start = LocalDate.now(ZONE).atStartOfDay();
		return start.format(DAY_START_FMT);
	}

	private void creditCoin(Integer uid, int amt, String bizType, String bizId, String taskCode, String remark)
			throws Exception {
		if (amt <= 0) {
			return;
		}
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before + amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode(taskCode == null ? "" : taskCode);
		ledger.setAdBoostFlag(0);
		ledger.setRemark(remark == null ? "" : remark);
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		appAccountDao.addCoinBalance(uid, amt);
	}

	private boolean deductCoin(Integer uid, int amt, String bizType, String bizId, String taskCode, String remark)
			throws Exception {
		if (amt <= 0) {
			return true;
		}
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return true;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		if (before < amt) {
			return false;
		}
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before - amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode(taskCode == null ? "" : taskCode);
		ledger.setAdBoostFlag(0);
		ledger.setRemark(remark == null ? "" : remark);
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return true;
		}
		if (appAccountDao.deductCoinBalance(uid, amt) <= 0) {
			throw new IllegalStateException("deduct coin balance failed uid=" + uid + " amt=" + amt);
		}
		return true;
	}
}
