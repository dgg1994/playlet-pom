package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.MemberContractReqEntity;
import com.playlet.internal.api.request.MemberPayNotifyReqEntity;
import com.playlet.internal.api.request.ProductReqEntity;
import com.playlet.internal.api.response.MemberOrderCreateRespEntity;
import com.playlet.internal.api.response.PayContractResult;
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
import com.playlet.internal.enums.MemberDeductStatusEnums;
import com.playlet.internal.enums.MemberOrderTypeEnums;
import com.playlet.internal.enums.MemberPayChannelEnums;
import com.playlet.internal.enums.MemberPayStatusEnums;
import com.playlet.internal.enums.MemberPayTypeEnums;
import com.playlet.internal.enums.PaySceneEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.MemberApiService;
import com.playlet.internal.service.support.CouponRedeemResult;
import com.playlet.internal.service.support.member.MemberAutoRenewSupport;
import com.playlet.internal.service.support.member.MemberPaidSupport;
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
	@Autowired
	private MemberAutoRenewSupport memberAutoRenewSupport;
	@Autowired
	private MemberPaidSupport memberPaidSupport;

	@Override
	public ResponseBase status(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		UserMemberEntity member = userMemberDao.findActiveByUid(uid);
		boolean active = member != null;
		UserMemberEntity row = userMemberDao.findByUid(uid);
		MemberStatusRespEntity resp = new MemberStatusRespEntity();
		resp.setIsMember(active);
		resp.setExpireTime(active ? member.getExpireTime() : null);
		resp.setAdFree(active);
		resp.setVipDramaFree(active);
		resp.setAutoRenew(row != null
				&& Integer.valueOf(MemberConstants.AUTO_RENEW_ON).equals(row.getAutoRenew()));
		resp.setNextDeductTime(row == null ? null : row.getNextDeductTime());
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
			// 查询会员套餐
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
			// 套餐是否为连续包月
			boolean autoRenew = MemberAutoRenewSupport.isAutoRenewProduct(product);
			if (autoRenew && MemberPayTypeEnums.COIN == payType) {
				return setResultError("连续包月套餐不支持金币购买，请选择现金支付");
			}

			// 计算本次开通前后的到期时间
			Date[] expireRange = memberPaidSupport.calcExpireRange(uid, product.getDurationDays());
			// 创建会员订单
			MemberOrderEntity order = new MemberOrderEntity();
			order.setOrderNo(OrderCodeFactory.getOrderCode(uid.longValue()));
			order.setUid(uid);
			order.setProductId(product.getId());
			order.setProductCode(product.getProductCode());
			order.setDurationDays(product.getDurationDays());
			order.setPayType(payType.getCode());
			order.setOrderType(MemberOrderTypeEnums.NEW.getIndex());
			order.setPeriodIndex(1);
			order.setDeductStatus(MemberDeductStatusEnums.INIT.getIndex());
			order.setExpireBefore(expireRange[0]);
			order.setExpireAfter(expireRange[1]);
			GenericityUtil.setDate(order);

			if (MemberPayTypeEnums.COIN == payType) {
				// 金币：校验价格 → 用券算价 → 扣币 → 订单直接已支付并开通 → 核销券
				int priceCoin = product.getPriceCoin() == null ? 0 : product.getPriceCoin();
				if (priceCoin <= 0) {
					return setResultError("该套餐不支持金币购买");
				}
				// 优惠券试算/校验结果 结果集
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
				// 开通或续期会员
				memberPaidSupport.grantOrRenewMember(order);
				if (redeem.hasCoupon()) {
					// 支付成功后核销
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
				// userCouponId 为空则无优惠；否则校验归属/状态/范围/币种并算应付
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

			// 构建返回值
			MemberOrderCreateRespEntity resp = toCreateResp(order);
			if (MemberPayTypeEnums.CASH == payType && autoRenew) {
				// 连续包月：首期同步拉起三方签约（支付宝返回 form / 微信返回签约参数）
				PaySceneEnums scene = PaySceneEnums.ofCode(productReqEntity.getPayScene()) == null
						? PaySceneEnums.PC : PaySceneEnums.ofCode(productReqEntity.getPayScene());
				PayContractResult contract = memberAutoRenewSupport.createAgreementAndSign(uid, product,
						order, order.getPayChannel(), scene, productReqEntity.getOpenId());
				resp.setContract(contract);
			} else if (MemberPayTypeEnums.CASH == payType) {
				resp.setPayInfo(tryPrepay(order.getPayChannel(), productReqEntity.getPayScene(),
						productReqEntity.getOpenId(), order.getOrderNo(), order.getPayAmount(), "会员套餐购买"));
			}
			return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("member order create failed uid={}", uid, e);
			throw new BaseException("会员下单失败", e);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase signAutoRenew(HttpServletRequest request, @RequestBody MemberContractReqEntity req) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (req == null || req.getProductId() == null) {
			return setResultError(I18nUtil.getMessage("param_not_null"));
		}
		String channel = req.getPayChannel() == null ? null : req.getPayChannel().trim();
		if (!MemberPayChannelEnums.isValid(channel) || MemberPayChannelEnums.BANK.getCode().equals(channel)) {
			return setResultError("连续包月仅支持 WECHAT / ALIPAY");
		}
		try {
			// 查询会员套餐
			MemberProductEntity product = memberProductDao.selectById(req.getProductId());
			if (product == null || !Integer.valueOf(MemberConstants.PRODUCT_STATUS_ON).equals(product.getStatus())) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (!MemberAutoRenewSupport.isAutoRenewProduct(product)) {
				return setResultError("该套餐不是连续包月套餐");
			}
			if (product.getPriceYuan() == null || product.getPriceYuan().compareTo(BigDecimal.ZERO) <= 0) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			// 复用下单逻辑生成首期订单，再拉起签约
			ProductReqEntity orderReq = new ProductReqEntity();
			orderReq.setProductId(req.getProductId());
			orderReq.setPayType(MemberPayTypeEnums.CASH.getCode());
			orderReq.setPayChannel(channel);
			orderReq.setPayScene(req.getPayScene());
			orderReq.setOpenId(req.getOpenId());
			orderReq.setUserCouponId(req.getUserCouponId());
			return createOrder(request, orderReq);
		} catch (Exception e) {
			log.error("member auto renew sign failed uid={}", uid, e);
			throw new BaseException("连续包月开通失败", e);
		}
	}

	@Override
	public ResponseBase autoRenewStatus(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return setResultSuccess(memberAutoRenewSupport.status(uid), I18nUtil.getMessage("base_success"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase unsignAutoRenew(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		try {
			// 用户主动解约：先调三方，再落本地状态
			memberAutoRenewSupport.unsign(uid);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("member auto renew unsign failed uid={}", uid, e);
			throw new BaseException("关闭连续包月失败", e);
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
			// 入账逻辑下沉到 MemberPaidSupport；开启自动续费由 MemberPaidEvent 监听处理
			MemberOrderEntity paid = memberPaidSupport.markPaid(orderNo, req.getThirdTradeNo());
			return setResultSuccess(paidNotifyData(paid), I18nUtil.getMessage("base_success"));
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

	/**
	 * 会员下单出参
	 * @param order
	 * @return
	 */
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
