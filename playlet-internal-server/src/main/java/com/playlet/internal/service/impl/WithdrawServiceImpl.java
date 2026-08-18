package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.request.OnePayWithdrawCallbackRequest;
import com.playlet.internal.api.request.WithdrawReqEntity;
import com.playlet.internal.api.response.WithdrawHomeRespEntity;
import com.playlet.internal.api.response.WithdrawHomeRespEntity.WithdrawAssetItemEntity;
import com.playlet.internal.api.response.WithdrawRecordItemEntity;
import com.playlet.internal.api.response.WithdrawSubmitRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.constants.WithdrawConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.dao.welfare.WithdrawConfigDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.entity.welfare.WithdrawConfigEntity;
import com.playlet.internal.enums.OnePayBindStatusEnums;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.service.WithdrawService;
import com.playlet.internal.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class WithdrawServiceImpl extends BaseApiService implements WithdrawService {

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
        long balance = nvl(account == null ? null : account.getCoinBalance());
        long frozen = nvl(account == null ? null : account.getFrozenCoinBalance());
        WithdrawHomeRespEntity resp = new WithdrawHomeRespEntity();
        resp.setCoinBalance(balance - frozen);
        resp.setFrozenCoinBalance(frozen);
        resp.setOnepayBindStatus(account == null ? OnePayBindStatusEnums.UNBOUND.getCode()
                : nvlInt(account.getOnepayBindStatus()));
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
    public ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request) {
        Integer uid = AppTokenUtil.resolveUid(request);
        if (uid == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        int points = query == null || query.getPoints() == null ? 0 : query.getPoints();
        if (points <= 0) {
            return setResultError(I18nUtil.getMessage("withdraw.points_invalid"));
        }
        AppAccountEntity account = appAccountDao.selectById(uid);
        if (account == null) {
            return setResultError(I18nUtil.getMessage("user.not_null"));
        }
        if (!Integer.valueOf(OnePayBindStatusEnums.BOUND.getCode()).equals(account.getOnepayBindStatus())
                || StringUtils.isEmpty(account.getOnepayAccount())) {
            return setResultError(I18nUtil.getMessage("withdraw.onepay_not_bound"));
        }
        // 可用积分
        long available = nvl(account.getCoinBalance()) - nvl(account.getFrozenCoinBalance());
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
                Integer used = userWithdrawOrderDao.sumPointsTodayByAsset(uid, cfg.getAssetCode(),
                        cfg.getNetwork(), todayStart());
                int usedAmt = used == null ? 0 : used;
                if (usedAmt + points > dayMax) {
                    return setResultError(I18nUtil.getMessage("withdraw.day_limit"));
                }
            }
        }

        String lockKey = RedisKeyConstants.WITHDRAW_SUBMIT_LOCK + uid;
        if (redisUtil.hasKey(lockKey)) {
            return setResultError(I18nUtil.getMessage("withdraw.too_frequent"));
        }
        redisUtil.set(lockKey, "1", RedisKeyConstants.WITHDRAW_SUBMIT_LOCK_SEC);

        String orderNo = "W" + OrderCodeFactory.getOrderCode(uid.longValue());
        try {
            // 只冻结，不扣 coin_balance
            if (appAccountDao.freezeCoinBalance(uid, points) <= 0) {
                return setResultError(I18nUtil.getMessage("withdraw.balance_not_enough"));
            }
            UserWithdrawOrderEntity order = buildOrder(uid, account.getOnepayAccount(), points, cfg, orderNo);
            GenericityUtil.setDate(order);
            userWithdrawOrderDao.insert(order);
            final Long orderId = order.getId();
			// 先提交本地冻结与提现单，再异步通知 OnePay，避免外部已打款但本地事务回滚
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
            log.info("withdraw submit uid={} orderNo={} points={}", uid, orderNo, points);
            return setResultSuccess( I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("withdraw submit failed uid={} orderNo={}", uid, orderNo, e);
            TransactionUtils.markRollbackOnly();
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
    }

    @Override
    public ResponseBase onepayCallback(@RequestBody OnePayWithdrawCallbackRequest query) {
        if (query == null || StringUtils.isEmpty(query.getOrderNo()) || query.getSuccess() == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        boolean success = query.getSuccess() == WithdrawConstants.CALLBACK_SUCCESS;
        log.info("onepay withdraw callback orderNo={} success={}", query.getOrderNo(), query.getSuccess());
        withdrawPayoutService.handleCallback(query.getOrderNo(), success,
                query.getThirdOrderNo(), query.getFailReason());
        return setResultSuccess(I18nUtil.getMessage("base_success"));
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

    private UserWithdrawOrderEntity buildOrder(Integer uid, String onepayAccount, int points,
                                               WithdrawConfigEntity cfg, String orderNo) {
        UserWithdrawOrderEntity order = new UserWithdrawOrderEntity();
        order.setOrderNo(orderNo);
        order.setUid(uid);
        order.setAssetCode(cfg == null ? WithdrawConstants.ASSET_ONEPAY : cfg.getAssetCode());
        order.setNetwork(cfg == null ? WithdrawConstants.NETWORK_ONEPAY : cfg.getNetwork());
        order.setWalletAddress(onepayAccount);
        order.setPointsAmt(points);
        order.setRate(cfg == null || cfg.getPointsPerUnit() == null ? 1 : cfg.getPointsPerUnit());
        order.setFeeAmt(cfg == null ? BigDecimal.ZERO : scale(cfg.getServiceFee()));
        order.setGrossAmt(BigDecimal.valueOf(points));
        order.setActualAmt(BigDecimal.valueOf(points));
        order.setStatus(WithdrawOrderStatusEnums.PENDING.getCode());
        return order;
    }

    private WithdrawSubmitRespEntity toSubmitResp(UserWithdrawOrderEntity order, AppAccountEntity account) {
        long balance = nvl(account == null ? null : account.getCoinBalance());
        long frozen = nvl(account == null ? null : account.getFrozenCoinBalance());
        WithdrawSubmitRespEntity resp = new WithdrawSubmitRespEntity();
        resp.setOrderNo(order.getOrderNo());
        resp.setAssetCode(order.getAssetCode());
        resp.setNetwork(order.getNetwork());
        resp.setStatus(order.getStatus());
        resp.setPointsAmt(order.getPointsAmt());
        resp.setActualAmt(order.getActualAmt() == null ? "0" : order.getActualAmt().stripTrailingZeros().toPlainString());
        resp.setCoinBalance(balance - frozen);
        resp.setFrozenCoinBalance(frozen);
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

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }

    private static int nvlInt(Integer v) {
        return v == null ? 0 : v;
    }

}
