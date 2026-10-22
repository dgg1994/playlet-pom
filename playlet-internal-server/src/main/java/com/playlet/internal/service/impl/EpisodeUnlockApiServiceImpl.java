package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.EpisodeUnlockAdReqEntity;
import com.playlet.internal.api.request.EpisodeUnlockCashCreateReqEntity;
import com.playlet.internal.api.request.EpisodeUnlockCashNotifyReqEntity;
import com.playlet.internal.api.request.EpisodeUnlockCoinReqEntity;
import com.playlet.internal.api.response.EpisodePlayAccessRespEntity;
import com.playlet.internal.api.response.EpisodeUnlockOrderRespEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.EpisodeUnlockConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.ad.AdRewardLogDao;
import com.playlet.internal.dao.ad.AdSlotConfigDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.UserEpisodeUnlockDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.ad.AdRewardLogEntity;
import com.playlet.internal.entity.ad.AdSlotConfigEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.member.EpisodeUnlockOrderEntity;
import com.playlet.internal.entity.member.UserEpisodeUnlockEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.EpisodeUnlockApiService;
import com.playlet.internal.service.support.CouponRedeemResult;
import com.playlet.internal.service.support.CouponRedeemSupport;
import com.playlet.internal.service.support.EpisodePlayAccessResult;
import com.playlet.internal.service.support.EpisodePlayAccessSupport;
import com.playlet.internal.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * C 端播放策略与剧集解锁（金币 / 现金 / 广告）。
 */
@Slf4j
@RestController
@CrossOrigin
public class EpisodeUnlockApiServiceImpl implements EpisodeUnlockApiService {

	@Autowired
	private EpisodePlayAccessSupport episodePlayAccessSupport;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private UserEpisodeUnlockDao userEpisodeUnlockDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private AdRewardLogDao adRewardLogDao;
	@Autowired
	private AdSlotConfigDao adSlotConfigDao;
	@Autowired
	private CouponRedeemSupport couponRedeemSupport;

	@Override
	public ResponseBase playAccess(@RequestParam("assetId") Integer assetId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		EpisodePlayAccessResult access = episodePlayAccessSupport.resolve(uid, assetId);
		return setResultSuccess(toAccessResp(access), I18nUtil.getMessage("base_success"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase unlockByCoin(HttpServletRequest request, @RequestBody EpisodeUnlockCoinReqEntity req) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (req == null || req.getAssetId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		try {
			DramaAssetEntity asset = requirePayAsset(req.getAssetId());
			EpisodePlayAccessResult access = episodePlayAccessSupport.resolve(uid, asset);
			if (access.isCanPlay()) {
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			if (!EpisodeUnlockConstants.BLOCK_NEED_PAY.equals(access.getBlockReason())) {
				return setResultError(I18nUtil.getMessage("episode.unlock_not_allowed"));
			}
			int priceCoin = access.getPriceCoin() == null ? 0 : access.getPriceCoin();
			if (priceCoin <= 0) {
				return setResultError(I18nUtil.getMessage("episode.unlock_coin_unavailable"));
			}
			CouponRedeemResult redeem = couponRedeemSupport.resolve(uid, req.getUserCouponId(),
					CouponScopeEnums.COIN_CONSUME.getCode(), CouponCurrencyEnums.COIN,
					BigDecimal.valueOf(priceCoin));
			int payCoin = redeem.getPayCoin() == null ? priceCoin : redeem.getPayCoin();
			String bizNo = OrderCodeFactory.getOrderCode(uid.longValue());
			if (payCoin > 0) {
				deductUnlockCoin(uid, payCoin, bizNo);
			}
			saveUnlock(uid, asset, EpisodeUnlockWayEnums.COIN.getCode(), payCoin, BigDecimal.ZERO, bizNo);
			if (redeem.hasCoupon()) {
				couponRedeemSupport.confirmUsed(uid, redeem.getCoupon().getId(),
						redeem.getCoupon().getTemplateId(), bizNo);
			}
			log.info("episode unlock coin ok uid={} assetId={} payCoin={} bizNo={} couponId={}",
					uid, asset.getId(), payCoin, bizNo,
					redeem.hasCoupon() ? redeem.getCoupon().getId() : null);

			EpisodeUnlockOrderRespEntity resp = new EpisodeUnlockOrderRespEntity();
			resp.setAssetId(asset.getId());
			resp.setDramaId(asset.getDramaId());
			resp.setUnlockWay(EpisodeUnlockWayEnums.COIN.getCode());
			resp.setBizNo(bizNo);
			resp.setPayStatus(MemberPayStatusEnums.PAID.getIndex());
			resp.setPayCoin(payCoin);
			resp.setDiscountAmt(redeem.getDiscountAmt());
			resp.setUserCouponId(redeem.hasCoupon() ? redeem.getCoupon().getId() : null);
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("episode unlock coin biz error uid={} assetId={}", uid, req.getAssetId(), e);
			throw e;
		} catch (Exception e) {
			log.error("episode unlock coin failed uid={} assetId={}", uid, req.getAssetId(), e);
			throw new BaseException(I18nUtil.getMessage("episode.unlock_failed"), e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase createCashOrder(HttpServletRequest request,
			@RequestBody EpisodeUnlockCashCreateReqEntity req) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (req == null || req.getAssetId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		try {
			DramaAssetEntity asset = requirePayAsset(req.getAssetId());
			EpisodePlayAccessResult access = episodePlayAccessSupport.resolve(uid, asset);
			if (access.isCanPlay()) {
				return setResultSuccess(alreadyUnlockedResp(asset, access), I18nUtil.getMessage("base_success"));
			}
			if (!EpisodeUnlockConstants.BLOCK_NEED_PAY.equals(access.getBlockReason())) {
				return setResultError(I18nUtil.getMessage("episode.unlock_not_allowed"));
			}
			if (access.getPriceYuan() == null || access.getPriceYuan().compareTo(BigDecimal.ZERO) <= 0) {
				return setResultError(I18nUtil.getMessage("episode.unlock_cash_unavailable"));
			}
			String channel = req.getPayChannel() == null ? null : req.getPayChannel().trim();
			if (!MemberPayChannelEnums.isValid(channel)) {
				return setResultError(I18nUtil.getMessage("episode.unlock_pay_channel_invalid"));
			}
			CouponRedeemResult redeem = couponRedeemSupport.resolve(uid, req.getUserCouponId(),
					CouponScopeEnums.COIN_CONSUME.getCode(), CouponCurrencyEnums.YUAN, access.getPriceYuan());

			EpisodeUnlockOrderEntity order = new EpisodeUnlockOrderEntity();
			order.setOrderNo(OrderCodeFactory.getOrderCode(uid.longValue()));
			order.setUid(uid);
			order.setDramaId(asset.getDramaId());
			order.setAssetId(asset.getId());
			order.setPayAmount(redeem.getPayAmount());
			order.setDiscountAmt(redeem.getDiscountAmt());
			order.setUserCouponId(redeem.hasCoupon() ? redeem.getCoupon().getId() : null);
			order.setPayStatus(MemberPayStatusEnums.WAIT.getIndex());
			GenericityUtil.setDate(order);
			episodeUnlockOrderDao.insert(order);
			log.info("episode unlock cash create uid={} assetId={} orderNo={} amount={} couponId={}",
					uid, asset.getId(), order.getOrderNo(), order.getPayAmount(), order.getUserCouponId());

			EpisodeUnlockOrderRespEntity resp = new EpisodeUnlockOrderRespEntity();
			resp.setAssetId(asset.getId());
			resp.setDramaId(asset.getDramaId());
			resp.setUnlockWay(EpisodeUnlockWayEnums.CASH.getCode());
			resp.setBizNo(order.getOrderNo());
			resp.setPayStatus(order.getPayStatus());
			resp.setPayAmount(order.getPayAmount());
			resp.setDiscountAmt(order.getDiscountAmt());
			resp.setUserCouponId(order.getUserCouponId());
			resp.setPayChannel(MemberPayChannelEnums.ofCode(channel).getCode());
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("episode unlock cash create biz error uid={} assetId={}", uid, req.getAssetId(), e);
			throw e;
		} catch (Exception e) {
			log.error("episode unlock cash create failed uid={} assetId={}", uid, req.getAssetId(), e);
			throw new BaseException(I18nUtil.getMessage("episode.unlock_failed"), e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase cashNotify(@RequestBody EpisodeUnlockCashNotifyReqEntity req) {
		if (req == null || StringUtils.isEmpty(req.getOrderNo())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String orderNo = req.getOrderNo().trim();
		try {
			EpisodeUnlockOrderEntity order = episodeUnlockOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (MemberPayStatusEnums.isPaid(order.getPayStatus())) {
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			if (!MemberPayStatusEnums.WAIT.getIndex().equals(order.getPayStatus())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			DramaAssetEntity asset = dramaAssetDao.findOnShelfById(order.getAssetId());
			if (asset == null) {
				return setResultError(I18nUtil.getMessage("video_not_up"));
			}

			Date now = new Date();
			order.setPayStatus(MemberPayStatusEnums.PAID.getIndex());
			order.setPayTime(now);
			order.setThirdTradeNo(StringUtils.isEmpty(req.getThirdTradeNo())
					? order.getOrderNo() : req.getThirdTradeNo().trim());
			order.setGmtModified(now);
			episodeUnlockOrderDao.updateById(order);

			UserEpisodeUnlockEntity exist = userEpisodeUnlockDao.findByUidAndAssetId(
					order.getUid(), order.getAssetId());
			if (exist == null) {
				saveUnlock(order.getUid(), asset, EpisodeUnlockWayEnums.CASH.getCode(),
						0, order.getPayAmount(), order.getOrderNo());
			}
			couponRedeemSupport.confirmUsed(order.getUid(), order.getUserCouponId(), null, orderNo);
			log.info("episode unlock cash notify ok uid={} assetId={} orderNo={}",
					order.getUid(), order.getAssetId(), orderNo);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("episode unlock cash notify biz error orderNo={}", orderNo, e);
			throw e;
		} catch (Exception e) {
			log.error("episode unlock cash notify failed orderNo={}", orderNo, e);
			throw new BaseException(I18nUtil.getMessage("episode.unlock_failed"), e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase unlockByAd(HttpServletRequest request, @RequestBody EpisodeUnlockAdReqEntity req) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (req == null || req.getAssetId() == null || StringUtils.isEmpty(req.getTransId())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String transId = req.getTransId().trim();
		try {
			DramaAssetEntity asset = dramaAssetDao.findOnShelfById(req.getAssetId());
			if (asset == null) {
				return setResultError(I18nUtil.getMessage("video_not_up"));
			}
			EpisodePlayAccessResult access = episodePlayAccessSupport.resolve(uid, asset);
			if (access.isCanPlay()) {
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			if (!EpisodeUnlockConstants.BLOCK_NEED_AD.equals(access.getBlockReason())) {
				return setResultError(I18nUtil.getMessage("episode.unlock_not_allowed"));
			}

			String slotCode = StringUtils.isEmpty(req.getSlotCode())
					? EpisodeUnlockConstants.AD_SLOT_REWARD_UNLOCK : req.getSlotCode().trim();
			AdSlotConfigEntity slot = adSlotConfigDao.findBySlotCode(slotCode);
			if (slot == null || !Integer.valueOf(1).equals(slot.getStatus())) {
				log.warn("episode unlock ad slot missing or off slotCode={}", slotCode);
			}

			AdRewardLogEntity existLog = adRewardLogDao.findByTransId(transId);
			if (existLog != null) {
				// 同一 transId 已处理：幂等返回
				if (Integer.valueOf(1).equals(existLog.getVerifyOk())
						&& uid.equals(existLog.getUid())
						&& req.getAssetId().equals(existLog.getAssetId())) {
					ensureUnlockRow(uid, asset, EpisodeUnlockWayEnums.AD.getCode(), 0, BigDecimal.ZERO, transId);
					return setResultSuccess(I18nUtil.getMessage("base_success"));
				}
				return setResultError(I18nUtil.getMessage("episode.ad_trans_used"));
			}

			// App SDK 验签由客户端完成；服务端落流水并按 transId 幂等（正式可接广告平台服务端校验）
			AdRewardLogEntity logRow = new AdRewardLogEntity();
			logRow.setUid(uid);
			logRow.setScene(EpisodeUnlockConstants.AD_SCENE_UNLOCK);
			logRow.setDramaId(asset.getDramaId());
			logRow.setAssetId(asset.getId());
			logRow.setSlotCode(slotCode);
			logRow.setTransId(transId);
			logRow.setVerifyOk(1);
			logRow.setEcpm(req.getEcpm());
			logRow.setRemark("ad unlock");
			GenericityUtil.setDate(logRow);
			try {
				adRewardLogDao.insert(logRow);
			} catch (DuplicateKeyException e) {
				return setResultError(I18nUtil.getMessage("episode.ad_trans_used"));
			}

			saveUnlock(uid, asset, EpisodeUnlockWayEnums.AD.getCode(), 0, BigDecimal.ZERO, transId);
			log.info("episode unlock ad ok uid={} assetId={} transId={}", uid, asset.getId(), transId);
			return setResultSuccess( I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("episode unlock ad failed uid={} assetId={}", uid, req.getAssetId(), e);
			throw new BaseException(I18nUtil.getMessage("episode.unlock_failed"), e);
		}
	}

	/** 付费解锁前置：已上架且 unlock_type=付费、非会员短剧由 access 再拦 */
	private DramaAssetEntity requirePayAsset(Integer assetId) {
		DramaAssetEntity asset = dramaAssetDao.findOnShelfById(assetId);
		if (asset == null) {
			throw new BaseException(I18nUtil.getMessage("video_not_up"));
		}
		if (!EpisodeUnlockTypeEnums.isPay(asset.getUnlockType())) {
			throw new BaseException(I18nUtil.getMessage("episode.unlock_not_allowed"));
		}
		return asset;
	}

	private void deductUnlockCoin(Integer uid, int amt, String bizNo) throws Exception {
		String bizType = CoinBizTypeEnums.EPISODE_UNLOCK.getName();
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizNo);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long balance = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account == null || account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		if (balance - frozen < amt) {
			throw new BaseException(I18nUtil.getMessage("sign_in_makeup_coin_not_enough"));
		}
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(balance);
		ledger.setBalanceAfter(balance - amt);
		ledger.setFrozenBefore(frozen);
		ledger.setFrozenAfter(frozen);
		ledger.setBizType(bizType);
		ledger.setBizId(bizNo);
		ledger.setTaskCode(EpisodeUnlockConstants.COIN_TASK_EPISODE_UNLOCK);
		ledger.setAdBoostFlag(0);
		ledger.setRemark("剧集解锁");
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		if (appAccountDao.deductCoinBalance(uid, amt) <= 0) {
			throw new BaseException(I18nUtil.getMessage("episode.unlock_coin_deduct_failed"));
		}
	}

	private void saveUnlock(Integer uid, DramaAssetEntity asset, String unlockWay,
			int costCoin, BigDecimal costYuan, String bizNo) throws Exception {
		UserEpisodeUnlockEntity row = new UserEpisodeUnlockEntity();
		row.setUid(uid);
		row.setDramaId(asset.getDramaId());
		row.setAssetId(asset.getId());
		row.setUnlockWay(unlockWay);
		row.setCostCoin(costCoin);
		row.setCostYuan(costYuan == null ? BigDecimal.ZERO : costYuan);
		row.setBizNo(bizNo);
		GenericityUtil.setDate(row);
		try {
			userEpisodeUnlockDao.insert(row);
		} catch (DuplicateKeyException e) {
			log.info("episode unlock already exists uid={} assetId={}", uid, asset.getId());
		}
	}

	private void ensureUnlockRow(Integer uid, DramaAssetEntity asset, String unlockWay,
			int costCoin, BigDecimal costYuan, String bizNo) throws Exception {
		UserEpisodeUnlockEntity exist = userEpisodeUnlockDao.findByUidAndAssetId(uid, asset.getId());
		if (exist == null) {
			saveUnlock(uid, asset, unlockWay, costCoin, costYuan, bizNo);
		}
	}

	private static EpisodePlayAccessRespEntity toAccessResp(EpisodePlayAccessResult access) {
		EpisodePlayAccessRespEntity resp = new EpisodePlayAccessRespEntity();
		resp.setAssetId(access.getAssetId());
		resp.setDramaId(access.getDramaId());
		resp.setSetNum(access.getSetNum());
		resp.setCanPlay(access.isCanPlay());
		resp.setUnlocked(access.isUnlocked());
		resp.setMember(access.isMember());
		resp.setMemberDrama(access.isMemberDrama());
		resp.setUnlockType(access.getUnlockType());
		resp.setPriceCoin(access.getPriceCoin());
		resp.setPriceYuan(access.getPriceYuan());
		resp.setBlockReason(access.getBlockReason());
		resp.setUnlockWay(access.getUnlockWay());
		return resp;
	}

	private static EpisodeUnlockOrderRespEntity alreadyUnlockedResp(DramaAssetEntity asset,
			EpisodePlayAccessResult access) {
		EpisodeUnlockOrderRespEntity resp = new EpisodeUnlockOrderRespEntity();
		resp.setAssetId(asset.getId());
		resp.setDramaId(asset.getDramaId());
		resp.setUnlockWay(access.getUnlockWay());
		resp.setPayStatus(MemberPayStatusEnums.PAID.getIndex());
		return resp;
	}

}
