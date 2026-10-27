package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.MemberPayNotifyReqEntity;
import com.playlet.internal.api.request.ProductReqEntity;
import com.playlet.internal.api.response.MemberOrderCreateRespEntity;
import com.playlet.internal.api.response.MemberStatusRespEntity;
import com.playlet.internal.api.response.PayPrepayResult;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.MemberConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.dao.member.MemberProductDao;
import com.playlet.internal.dao.member.UserMemberDao;
import com.playlet.internal.dao.welfare.UserCoinLedgerDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.entity.member.MemberProductEntity;
import com.playlet.internal.entity.member.UserMemberEntity;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.CouponCurrencyEnums;
import com.playlet.internal.enums.CouponScopeEnums;
import com.playlet.internal.enums.MemberPayChannelEnums;
import com.playlet.internal.enums.MemberPayStatusEnums;
import com.playlet.internal.enums.MemberPayTypeEnums;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.MemberApiService;
import com.playlet.internal.service.support.CouponRedeemResult;
import com.playlet.internal.service.support.CouponRedeemSupport;
import com.playlet.internal.service.support.pay.PaySupport;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * C 端会员：状态、套餐、下单（金币/现金）、现金支付确认。
 */
@Slf4j
@RestController
@CrossOrigin
public class MemberApiServiceImpl implements MemberApiService {

	@Autowired
	private UserMemberDao userMemberDao;
	@Autowired
	private MemberProductDao memberProductDao;
	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private CouponRedeemSupport couponRedeemSupport;
	@Autowired
	private PaySupport paySupport;

	@Override
	public ResponseBase status(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		UserMemberEntity member = userMemberDao.findActiveByUid(uid);
		boolean active = member != null;
		MemberStatusRespEntity resp = new MemberStatusRespEntity();
		resp.setIsMember(active);
		resp.setExpireTime(active ? member.getExpireTime() : null);
		resp.setAdFree(active);
		resp.setVipDramaFree(active);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase getMemberProducts() {
		List<MemberProductEntity> list = memberProductDao.findEnabledList();
		if (list == null) {
			list = Collections.emptyList();
		}
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase createOrder(HttpServletRequest request, @RequestBody ProductReqEntity productReqEntity) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (productReqEntity == null || productReqEntity.getProductId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		try {
			MemberProductEntity product = memberProductDao.selectById(productReqEntity.getProductId());
			if (product == null || !Integer.valueOf(MemberConstants.PRODUCT_STATUS_ON).equals(product.getStatus())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (product.getDurationDays() == null || product.getDurationDays() <= 0) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}

			MemberPayTypeEnums payType = MemberPayTypeEnums.ofCode(productReqEntity.getPayType());
			if (payType == null) {
				payType = MemberPayTypeEnums.CASH;
			}

			Date[] expireRange = calcExpireRange(uid, product.getDurationDays());
			MemberOrderEntity order = new MemberOrderEntity();
			order.setOrderNo(OrderCodeFactory.getOrderCode(uid.longValue()));
			order.setUid(uid);
			order.setProductId(product.getId());
			order.setProductCode(product.getProductCode());
			order.setDurationDays(product.getDurationDays());
			order.setPayType(payType.getCode());
			order.setExpireBefore(expireRange[0]);
			order.setExpireAfter(expireRange[1]);
			GenericityUtil.setDate(order);

			if (MemberPayTypeEnums.COIN == payType) {
				// 金币：校验价格 → 用券算价 → 扣币 → 订单直接已支付并开通 → 核销券
				int priceCoin = product.getPriceCoin() == null ? 0 : product.getPriceCoin();
				if (priceCoin <= 0) {
					return setResultError("该套餐不支持金币购买");
				}
				CouponRedeemResult redeem = couponRedeemSupport.resolve(uid, productReqEntity.getUserCouponId(),
						CouponScopeEnums.MEMBER.getCode(), CouponCurrencyEnums.COIN, BigDecimal.valueOf(priceCoin));
				int payCoin = redeem.getPayCoin() == null ? priceCoin : redeem.getPayCoin();
				order.setPayCoin(payCoin);
				order.setPayAmount(BigDecimal.ZERO);
				order.setDiscountAmt(redeem.getDiscountAmt());
				order.setUserCouponId(redeem.hasCoupon() ? redeem.getCoupon().getId() : null);
				order.setPayChannel(null);
				order.setPayStatus(MemberPayStatusEnums.PAID.getIndex());
				order.setPayTime(new Date());
				order.setThirdTradeNo(order.getOrderNo());
				memberOrderDao.insert(order);
				if (payCoin > 0) {
					deductMemberCoin(uid, payCoin, order.getOrderNo());
				}
				grantOrRenewMember(order);
				if (redeem.hasCoupon()) {
					couponRedeemSupport.confirmUsed(uid, redeem.getCoupon().getId(),
							redeem.getCoupon().getTemplateId(), order.getOrderNo());
				}
				log.info("member order coin ok uid={} orderNo={} payCoin={} couponId={}",
						uid, order.getOrderNo(), payCoin, order.getUserCouponId());
			} else {
				// 现金：必须选渠道，待支付；用券后写折后实付，支付成功 notify 再核销
				String channel = productReqEntity.getPayChannel() == null
						? null : productReqEntity.getPayChannel().trim();
				if (!MemberPayChannelEnums.isValid(channel)) {
					return setResultError("现金支付请选择 WECHAT / ALIPAY / BANK");
				}
				if (product.getPriceYuan() == null || product.getPriceYuan().compareTo(BigDecimal.ZERO) <= 0) {
					return setResultError(I18nUtil.getMessage("base_error"));
				}
				CouponRedeemResult redeem = couponRedeemSupport.resolve(uid, productReqEntity.getUserCouponId(),
						CouponScopeEnums.MEMBER.getCode(), CouponCurrencyEnums.YUAN, product.getPriceYuan());
				order.setPayChannel(MemberPayChannelEnums.ofCode(channel).getCode());
				order.setPayAmount(redeem.getPayAmount());
				order.setDiscountAmt(redeem.getDiscountAmt());
				order.setUserCouponId(redeem.hasCoupon() ? redeem.getCoupon().getId() : null);
				order.setPayCoin(0);
				order.setPayStatus(MemberPayStatusEnums.WAIT.getIndex());
				memberOrderDao.insert(order);
				log.info("member order cash create uid={} orderNo={} channel={} payAmount={} couponId={}",
						uid, order.getOrderNo(), order.getPayChannel(), order.getPayAmount(), order.getUserCouponId());
			}

			MemberOrderCreateRespEntity resp = toCreateResp(order);
			if (MemberPayTypeEnums.CASH == payType) {
				resp.setPayInfo(tryPrepay(order.getPayChannel(), productReqEntity.getPayScene(),
						productReqEntity.getOpenId(), order.getOrderNo(), order.getPayAmount(), "会员套餐购买"));
			}
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("member order create biz error uid={}", uid, e);
			throw e;
		} catch (Exception e) {
			log.error("member order create failed uid={}", uid, e);
			throw new BaseException("会员下单失败", e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase payNotify(@RequestBody MemberPayNotifyReqEntity req) {
		if (req == null || StringUtils.isEmpty(req.getOrderNo())) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String orderNo = req.getOrderNo().trim();
		try {
			MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			// 金币单创建时已支付；现金单走本回调
			if (MemberPayStatusEnums.isPaid(order.getPayStatus())) {
				return setResultSuccess(paidNotifyData(order), I18nUtil.getMessage("base_success"));
			}
			if (!MemberPayTypeEnums.isCash(order.getPayType())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (!MemberPayStatusEnums.WAIT.getIndex().equals(order.getPayStatus())) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}

			Date[] expireRange = calcExpireRange(order.getUid(), order.getDurationDays());
			Date now = new Date();
			order.setPayStatus(MemberPayStatusEnums.PAID.getIndex());
			order.setPayTime(now);
			order.setThirdTradeNo(StringUtils.isEmpty(req.getThirdTradeNo())
					? order.getOrderNo() : req.getThirdTradeNo().trim());
			order.setExpireBefore(expireRange[0]);
			order.setExpireAfter(expireRange[1]);
			order.setGmtModified(now);
			memberOrderDao.updateById(order);
			grantOrRenewMember(order);
			// 现金支付成功后核销优惠券（幂等）
			couponRedeemSupport.confirmUsed(order.getUid(), order.getUserCouponId(), null, orderNo);
			log.info("member pay notify ok uid={} orderNo={} channel={} expireAfter={}",
					order.getUid(), orderNo, order.getPayChannel(), order.getExpireAfter());
			return setResultSuccess(paidNotifyData(order), I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("member pay notify biz error orderNo={}", orderNo, e);
			throw e;
		} catch (Exception e) {
			log.error("member pay notify failed orderNo={}", orderNo, e);
			throw new BaseException("会员支付确认失败", e);
		}
	}

	/**
	 * 现金下单后拉三方预支付；失败不影响订单本身，返回失败原因由前端再调 /api/pay/prepay 重试。
	 */
	private PayPrepayResult tryPrepay(String channel, String payScene, String openId,
			String orderNo, BigDecimal amountYuan, String subject) {
		if (!paySupport.available(channel)) {
			return null;
		}
		try {
			PaySceneEnums scene = PaySceneEnums.ofCode(payScene) == null
					? PaySceneEnums.PC : PaySceneEnums.ofCode(payScene);
			return paySupport.prepay(channel, scene, orderNo, amountYuan, subject, openId);
		} catch (Exception e) {
			log.error("member prepay failed orderNo={} channel={}", orderNo, channel, e);
			return PayPrepayResult.fail(channel, "获取支付参数失败：" + e.getMessage());
		}
	}

	/** 扣金币并记流水；余额不足抛业务异常。 */
	private void deductMemberCoin(Integer uid, int amt, String orderNo) throws Exception {
		String bizType = CoinBizTypeEnums.MEMBER_BUY.getName();
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, orderNo);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long balance = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		long frozen = account == null || account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance();
		if (balance - frozen < amt) {
			throw new BaseException("金币不足");
		}
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(-amt);
		ledger.setBalanceBefore(balance);
		ledger.setBalanceAfter(balance - amt);
		ledger.setFrozenBefore(frozen);
		ledger.setFrozenAfter(frozen);
		ledger.setBizType(bizType);
		ledger.setBizId(orderNo);
		ledger.setTaskCode(MemberConstants.COIN_TASK_MEMBER_BUY);
		ledger.setAdBoostFlag(0);
		ledger.setRemark("购买会员");
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		if (appAccountDao.deductCoinBalance(uid, amt) <= 0) {
			throw new BaseException("金币扣减失败");
		}
	}

	private void grantOrRenewMember(MemberOrderEntity order) throws Exception {
		UserMemberEntity exist = userMemberDao.findByUid(order.getUid());
		Date now = new Date();
		if (exist == null) {
			UserMemberEntity insert = new UserMemberEntity();
			insert.setUid(order.getUid());
			insert.setStatus(MemberConstants.MEMBER_STATUS_ON);
			insert.setExpireTime(order.getExpireAfter());
			insert.setLastOrderNo(order.getOrderNo());
			GenericityUtil.setDate(insert);
			userMemberDao.insert(insert);
			return;
		}
		exist.setStatus(MemberConstants.MEMBER_STATUS_ON);
		exist.setExpireTime(order.getExpireAfter());
		exist.setLastOrderNo(order.getOrderNo());
		exist.setGmtModified(now);
		userMemberDao.updateById(exist);
	}

	private Date[] calcExpireRange(Integer uid, int durationDays) {
		Date now = new Date();
		UserMemberEntity active = userMemberDao.findActiveByUid(uid);
		Date expireBefore;
		Date base;
		if (active != null && active.getExpireTime() != null && active.getExpireTime().after(now)) {
			expireBefore = active.getExpireTime();
			base = active.getExpireTime();
		} else {
			expireBefore = now;
			base = now;
		}
		Date expireAfter = Date.from(base.toInstant().plus(durationDays, ChronoUnit.DAYS));
		return new Date[] { expireBefore, expireAfter };
	}

	private static MemberOrderCreateRespEntity toCreateResp(MemberOrderEntity order) {
		MemberOrderCreateRespEntity resp = new MemberOrderCreateRespEntity();
		resp.setOrderNo(order.getOrderNo());
		resp.setPayType(order.getPayType());
		resp.setPayChannel(order.getPayChannel());
		resp.setPayAmount(order.getPayAmount());
		resp.setPayCoin(order.getPayCoin());
		resp.setUserCouponId(order.getUserCouponId());
		resp.setDiscountAmt(order.getDiscountAmt());
		resp.setPayStatus(order.getPayStatus());
		resp.setDurationDays(order.getDurationDays());
		resp.setExpireBefore(order.getExpireBefore());
		resp.setExpireAfter(order.getExpireAfter());
		return resp;
	}

	private static Map<String, Object> paidNotifyData(MemberOrderEntity order) {
		Map<String, Object> data = new HashMap<>(4);
		data.put("orderNo", order.getOrderNo());
		data.put("payStatus", order.getPayStatus());
		data.put("expireAfter", order.getExpireAfter());
		return data;
	}
}
