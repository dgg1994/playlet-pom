package com.playlet.internal.service.support.member;

import com.playlet.internal.api.response.MemberAutoRenewStatusRespEntity;
import com.playlet.internal.api.response.PayContractResult;
import com.playlet.internal.api.response.PayDeductResult;
import com.playlet.internal.constants.MemberConstants;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.dao.member.MemberProductDao;
import com.playlet.internal.dao.member.UserMemberDao;
import com.playlet.internal.dao.member.UserPayAgreementDao;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.entity.member.MemberProductEntity;
import com.playlet.internal.entity.member.UserMemberEntity;
import com.playlet.internal.entity.member.UserPayAgreementEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.event.MemberPaidEvent;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.support.pay.PaySupport;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 连续包月：签约、自动续费代扣、解约。
 * <p>
 * 首期为「签约 + 普通下单支付」，后续每期由 {@link #renew} 主动按协议代扣。
 */
@Slf4j
@Component
public class MemberAutoRenewSupport {

	/** 每批处理上限，避免长事务 */
	private static final int BATCH_LIMIT = 200;

	@Autowired
	private UserPayAgreementDao agreementDao;
	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private MemberProductDao memberProductDao;
	@Autowired
	private UserMemberDao userMemberDao;
	@Autowired
	private PaySupport paySupport;
	@Autowired
	private MemberPaidSupport memberPaidSupport;

	// ------------------------------------------------------------- 价格与周期

	/** 每期续费价；未单独配置取售价 */
	public BigDecimal renewPrice(MemberProductEntity product) {
		if (product.getRenewPriceYuan() != null && product.getRenewPriceYuan().compareTo(BigDecimal.ZERO) > 0) {
			return product.getRenewPriceYuan();
		}
		return product.getPriceYuan();
	}

	/** 每期天数；未单独配置取开通天数 */
	public Integer periodDays(MemberProductEntity product) {
		Integer days = product.getRenewPeriodDays();
		if (days == null || days <= 0) {
			days = product.getDurationDays();
		}
		return days == null || days <= 0 ? 30 : days;
	}

	// ----------------------------------------------------------------- 签约

	/**
	 * 连续包月首期：创建待签约协议并拉起三方签约。
	 *
	 * @param order 已落库的待支付首期订单（externalAgreementNo 取该订单号，保证重试幂等）
	 */
	public PayContractResult createAgreementAndSign(Integer uid, MemberProductEntity product,
			MemberOrderEntity order, String channel, PaySceneEnums scene, String openId) throws Exception {
		UserPayAgreementEntity agreement = agreementDao.findByExternalAgreementNo(order.getOrderNo());
		if (agreement == null) {
			agreement = new UserPayAgreementEntity();
			agreement.setUid(uid);
			agreement.setPayChannel(channel);
			agreement.setPayScene(scene == null ? PaySceneEnums.PC.getCode() : scene.getCode());
			agreement.setProductId(product.getId());
			agreement.setExternalAgreementNo(order.getOrderNo());
			agreement.setStatus(PayAgreementStatusEnums.WAIT.getIndex());
			agreement.setPeriodDays(periodDays(product));
			agreement.setRenewPrice(renewPrice(product));
			agreement.setRemark("首期签约");
			GenericityUtil.setDate(agreement);
			agreementDao.insert(agreement);
		}
		order.setAgreementId(agreement.getId());
		order.setOrderType(MemberOrderTypeEnums.NEW.getIndex());
		order.setPeriodIndex(1);
		memberOrderDao.updateById(order);

		if (!paySupport.contractAvailable(channel)) {
			log.warn("contract unavailable channel={} orderNo={} uid={}", channel, order.getOrderNo(), uid);
			return PayContractResult.fail(channel, order.getOrderNo(),
					"该渠道未开通连续包月（微信需 pay.wechat.papay-plan-id，支付宝需开通周期扣款）");
		}
		// 每期周期由本地 period_days 维护；支付宝走通用代扣，不下发周期规则，扣款一律由本服务发起
		return paySupport.contractSign(channel, scene, order.getOrderNo(), product.getProductName(),
				renewPrice(product), openId);
	}

	/** 三方签约成功回调：置已签约并开启自动续费 */
	@Transactional(rollbackFor = Exception.class)
	public void markSigned(String externalAgreementNo, String agreementNo, String thirdUserId) {
		UserPayAgreementEntity agreement = agreementDao.findByExternalAgreementNo(externalAgreementNo);
		if (agreement == null) {
			throw new BaseException("签约记录不存在：" + externalAgreementNo);
		}
		if (PayAgreementStatusEnums.isSigned(agreement.getStatus())) {
			return;
		}
		agreement.setStatus(PayAgreementStatusEnums.SIGNED.getIndex());
		agreement.setAgreementNo(agreementNo);
		agreement.setThirdUserId(thirdUserId);
		agreement.setSignTime(new Date());
		agreement.setGmtModified(new Date());
		agreementDao.updateById(agreement);
		log.info("agreement signed uid={} externalNo={} agreementNo={}", agreement.getUid(),
				externalAgreementNo, agreementNo);
		applyAutoRenewOnMember(agreement);
	}

	/**
	 * 监听会员入账事件：首期支付成功后若协议已签约则开启自动续费。
	 * <p>
	 * 同步事件（不带 @Async），异常会随发布方事务一起回滚，与改造前行为一致。
	 */
	@EventListener
	public void onMemberPaid(MemberPaidEvent event) {
		if (event == null || event.getOrder() == null) {
			return;
		}
		applyAutoRenewAfterPaid(event.getOrder());
	}

	/** 首期支付成功后：若协议已签约则开启自动续费 */
	@Transactional(rollbackFor = Exception.class)
	public void applyAutoRenewAfterPaid(MemberOrderEntity order) {
		if (order == null || order.getAgreementId() == null) {
			return;
		}
		UserPayAgreementEntity agreement = agreementDao.selectById(order.getAgreementId());
		if (agreement == null || !PayAgreementStatusEnums.isSigned(agreement.getStatus())) {
			return;
		}
		applyAutoRenewOnMember(agreement);
	}

	private void applyAutoRenewOnMember(UserPayAgreementEntity agreement) {
		UserMemberEntity member = userMemberDao.findByUid(agreement.getUid());
		if (member == null) {
			return;
		}
		member.setAutoRenew(MemberConstants.AUTO_RENEW_ON);
		member.setAgreementId(agreement.getId());
		member.setRenewProductId(agreement.getProductId());
		member.setNextDeductTime(member.getExpireTime());
		member.setGmtModified(new Date());
		userMemberDao.updateById(member);
		log.info("auto renew enabled uid={} agreementId={} nextDeductTime={}",
				agreement.getUid(), agreement.getId(), member.getNextDeductTime());
	}

	// ----------------------------------------------------------------- 解约

	/** 用户主动解约：先调三方，再落本地状态 */
	@Transactional(rollbackFor = Exception.class)
	public void unsign(Integer uid) {
		UserPayAgreementEntity agreement = agreementDao.findSignedLatestByUid(uid);
		if (agreement == null) {
			throw new BaseException("当前没有已签约的自动续费协议");
		}
		String failReason = null;
		try {
			boolean ok = paySupport.contractUnsign(agreement.getPayChannel(),
					agreement.getExternalAgreementNo(), agreement.getAgreementNo());
			if (!ok) {
				failReason = "三方解约失败，已强制本地解约";
			}
		} catch (Exception e) {
			log.error("unsign third failed agreementId={}", agreement.getId(), e);
			failReason = "三方解约异常，已强制本地解约：" + e.getMessage();
		}
		markUnsigned(agreement, failReason);
	}

	/** 三方解约回调或本地强制解约 */
	@Transactional(rollbackFor = Exception.class)
	public void markUnsigned(UserPayAgreementEntity agreement, String reason) {
		if (agreement == null) {
			return;
		}
		agreement.setStatus(PayAgreementStatusEnums.UNSIGNED.getIndex());
		agreement.setCancelTime(new Date());
		if (!StringUtils.isEmpty(reason)) {
			agreement.setRemark(cut(reason, 200));
		}
		agreement.setGmtModified(new Date());
		agreementDao.updateById(agreement);

		UserMemberEntity member = userMemberDao.findByUid(agreement.getUid());
		if (member != null) {
			member.setAutoRenew(MemberConstants.AUTO_RENEW_OFF);
			member.setAgreementId(null);
			member.setNextDeductTime(null);
			member.setGmtModified(new Date());
			userMemberDao.updateById(member);
		}
		log.info("agreement unsigned uid={} agreementId={} reason={}",
				agreement.getUid(), agreement.getId(), reason);
	}

	// ----------------------------------------------------------------- 状态

	public MemberAutoRenewStatusRespEntity status(Integer uid) {
		MemberAutoRenewStatusRespEntity resp = new MemberAutoRenewStatusRespEntity();
		resp.setAutoRenew(Boolean.FALSE);
		UserPayAgreementEntity agreement = agreementDao.findSignedLatestByUid(uid);
		if (agreement == null) {
			List<UserPayAgreementEntity> all = agreementDao.findByUid(uid);
			if (all != null && !all.isEmpty()) {
				UserPayAgreementEntity latest = all.get(0);
				PayAgreementStatusEnums st = PayAgreementStatusEnums.ofIndex(latest.getStatus());
				resp.setAgreementStatus(st == null ? null : st.name());
				resp.setPayChannel(latest.getPayChannel());
			}
			return resp;
		}
		PayAgreementStatusEnums st = PayAgreementStatusEnums.ofIndex(agreement.getStatus());
		resp.setAgreementStatus(st == null ? null : st.name());
		resp.setPayChannel(agreement.getPayChannel());
		resp.setAgreementNo(agreement.getAgreementNo());
		resp.setProductId(agreement.getProductId());
		resp.setRenewPrice(agreement.getRenewPrice());
		resp.setPeriodDays(agreement.getPeriodDays());
		UserMemberEntity member = userMemberDao.findByUid(uid);
		resp.setAutoRenew(member != null
				&& Integer.valueOf(MemberConstants.AUTO_RENEW_ON).equals(member.getAutoRenew()));
		resp.setNextDeductTime(member == null ? null : member.getNextDeductTime());
		if (agreement.getProductId() != null) {
			MemberProductEntity product = memberProductDao.selectById(agreement.getProductId());
			if (product != null) {
				resp.setProductName(product.getProductName());
			}
		}
		return resp;
	}

	// ------------------------------------------------------------- 自动续费

	/** 扫描到期协议并代扣；返回 [成功数, 失败数] */
	public int[] processDue() {
		List<UserPayAgreementEntity> dueList = agreementDao.findDueRenewals(new Date(), BATCH_LIMIT);
		if (dueList == null || dueList.isEmpty()) {
			return new int[] { 0, 0 };
		}
		int ok = 0;
		int fail = 0;
		for (UserPayAgreementEntity agreement : dueList) {
			try {
				if (renew(agreement)) {
					ok++;
				} else {
					fail++;
				}
			} catch (Exception e) {
				fail++;
				log.error("auto renew error agreementId={} uid={}", agreement.getId(), agreement.getUid(), e);
			}
		}
		log.info("auto renew batch due={} success={} fail={}", dueList.size(), ok, fail);
		return new int[] { ok, fail };
	}

	/**
	 * 一期代扣：建续费订单 → 调三方 → 成功则 {@link OrderPaidSupport#markPaid} 续期并重排下次扣款时间。
	 *
	 * @return 是否扣款成功
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean renew(UserPayAgreementEntity agreement) throws Exception {
		MemberProductEntity product = memberProductDao.selectById(agreement.getProductId());
		if (product == null || product.getStatus() == null
				|| !Integer.valueOf(MemberConstants.PRODUCT_STATUS_ON).equals(product.getStatus())) {
			markUnsigned(agreement, "套餐已下架，自动关闭续费");
			return false;
		}
		// 该协议已有最大期数
		int periodIndex = nextPeriodIndex(agreement);
		String orderNo = OrderCodeFactory.getOrderCode(agreement.getUid().longValue());
		BigDecimal price = agreement.getRenewPrice() == null ? renewPrice(product) : agreement.getRenewPrice();
		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
			log.error("auto renew price invalid agreementId={}", agreement.getId());
			return false;
		}

		MemberOrderEntity order = new MemberOrderEntity();
		order.setOrderNo(orderNo);
		order.setUid(agreement.getUid());
		order.setProductId(product.getId());
		order.setProductCode(product.getProductCode());
		order.setOrderType(MemberOrderTypeEnums.RENEW.getIndex());
		order.setAgreementId(agreement.getId());
		order.setPeriodIndex(periodIndex);
		order.setDurationDays(periodDays(product));
		order.setPayType(MemberPayTypeEnums.CASH.getCode());
		order.setPayChannel(agreement.getPayChannel());
		order.setPayAmount(price);
		order.setPayCoin(0);
		order.setDiscountAmt(BigDecimal.ZERO);
		order.setPayStatus(MemberPayStatusEnums.WAIT.getIndex());
		order.setDeductStatus(MemberDeductStatusEnums.INIT.getIndex());
		GenericityUtil.setDate(order);
		try {
			memberOrderDao.insert(order);
		} catch (DuplicateKeyException e) {
			log.warn("auto renew duplicated period agreementId={} period={}", agreement.getId(), periodIndex);
			return false;
		}

		PayDeductResult result;
		try {
			result = paySupport.contractPay(agreement.getPayChannel(), agreement.getAgreementNo(),
					orderNo, price, product.getProductName());
		} catch (Exception e) {
			result = PayDeductResult.fail(agreement.getPayChannel(), orderNo, "DEDUCT_ERROR", cut(e.getMessage(), 200));
		}
		if (!result.isSuccess()) {
			order.setDeductStatus(MemberDeductStatusEnums.FAIL.getIndex());
			order.setPayStatus(MemberPayStatusEnums.CLOSED.getIndex());
			order.setRemark(cut(result.getErrMsg(), 200));
			order.setGmtModified(new Date());
			memberOrderDao.updateById(order);
			log.warn("auto renew deduct failed orderNo={} err={}", orderNo, result.getErrMsg());
			return false;
		}

		// 扣款成功：复用会员入账逻辑开通/续期
		MemberOrderEntity paid = memberPaidSupport.markPaid(orderNo, result.getThirdTradeNo());
		if (paid != null) {
			paid.setDeductStatus(MemberDeductStatusEnums.SUCCESS.getIndex());
			paid.setThirdTradeNo(result.getThirdTradeNo());
			paid.setRemark("自动续费第 " + periodIndex + " 期");
			paid.setGmtModified(new Date());
			memberOrderDao.updateById(paid);
		}
		reschedule(agreement);
		return true;
	}

	/** 下一期：该协议已有最大期数 + 1 */
	private int nextPeriodIndex(UserPayAgreementEntity agreement) {
		List<MemberOrderEntity> orders = memberOrderDao.findByAgreementId(agreement.getId());
		int max = 1;
		if (orders != null) {
			for (MemberOrderEntity order : orders) {
				if (order.getPeriodIndex() != null && order.getPeriodIndex() > max) {
					max = order.getPeriodIndex();
				}
			}
		}
		return max + 1;
	}

	/** 按会员最新到期时间重排下次扣款日 */
	private void reschedule(UserPayAgreementEntity agreement) {
		UserMemberEntity member = userMemberDao.findByUid(agreement.getUid());
		Date next = member == null ? null : member.getExpireTime();
		agreement.setNextDeductTime(next);
		agreement.setGmtModified(new Date());
		agreementDao.updateById(agreement);
		if (member != null) {
			member.setNextDeductTime(next);
			member.setGmtModified(new Date());
			userMemberDao.updateById(member);
		}
	}

	/** 渠道是否允许签约 */
	public boolean supportAutoRenew(String channel) {
		return MemberPayChannelEnums.ofCode(channel) != null
				&& paySupport.contractAvailable(channel);
	}

	/** 套餐是否为连续包月 */
	public static boolean isAutoRenewProduct(MemberProductEntity product) {
		return product != null && MemberProductTypeEnums.isAutoRenew(product.getProductType());
	}

	private static String cut(String text, int max) {
		if (StringUtils.isEmpty(text)) {
			return null;
		}
		return text.length() <= max ? text : text.substring(0, max);
	}
}
