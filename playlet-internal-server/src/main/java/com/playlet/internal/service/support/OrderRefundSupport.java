package com.playlet.internal.service.support;

import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.entity.member.EpisodeUnlockOrderEntity;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.enums.MemberPayStatusEnums;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.support.pay.PayRefundResult;
import com.playlet.internal.service.support.pay.PaySupport;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单退款：先调三方原路退回，再标记本地已退款（会员/单集解锁共用）。
 */
@Slf4j
@Component
public class OrderRefundSupport {

	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;
	@Autowired
	private PaySupport paySupport;

	/** 按商品类型退款；渠道已配置时先走渠道退款，失败直接抛出（不打退款标记） */
	public void markRefunded(String orderNo, RechargeProductTypeEnums type, String remark) {
		refundByChannel(orderNo, type);
		markRefundedLocal(orderNo, type, remark);
	}

	/** 仅改本地状态，不再调用渠道（供退款回调补单使用，避免重复调渠道退款） */
	public void markRefundedLocal(String orderNo, RechargeProductTypeEnums type, String remark) {
		if (type == RechargeProductTypeEnums.MEMBER) {
			refundMember(orderNo, remark);
		} else {
			refundEpisode(orderNo, remark);
		}
	}

	/** 判断订单归属的商品类型；两表都没有返回 null */
	public RechargeProductTypeEnums resolveType(String orderNo) {
		if (StringUtils.isEmpty(orderNo)) {
			return null;
		}
		if (memberOrderDao.findByOrderNo(orderNo) != null) {
			return RechargeProductTypeEnums.MEMBER;
		}
		if (episodeUnlockOrderDao.findByOrderNo(orderNo) != null) {
			return RechargeProductTypeEnums.EPISODE;
		}
		return null;
	}

	/**
	 * 调用渠道原路退回。渠道未配置（如 BANK、或 pay 开关关闭）时直接跳过保持原有本地行为；
	 * 渠道返回明确失败时抛业务异常，上层据此不打退款标记。
	 */
	private void refundByChannel(String orderNo, RechargeProductTypeEnums type) {
		String channel;
		String thirdTradeNo;
		BigDecimal payAmount;
		if (type == RechargeProductTypeEnums.MEMBER) {
			MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			channel = order.getPayChannel();
			thirdTradeNo = order.getThirdTradeNo();
			payAmount = order.getPayAmount();
		} else {
			EpisodeUnlockOrderEntity order = episodeUnlockOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			channel = order.getPayChannel();
			thirdTradeNo = order.getThirdTradeNo();
			payAmount = order.getPayAmount();
		}
		if (StringUtils.isEmpty(channel) || !paySupport.available(channel)) {
			log.warn("refund skip channel orderNo={} channel={} (渠道未配置或不支持在线退款)", orderNo, channel);
			return;
		}
		if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.warn("refund skip channel orderNo={} amount={}", orderNo, payAmount);
			return;
		}
		// 退款单号固定由订单号派生，保证重试幂等
		String refundNo = "R" + orderNo;
		PayRefundResult result = paySupport.refund(channel, orderNo, thirdTradeNo, refundNo,
				payAmount, payAmount, "用户退款");
		if (result == null || !result.isSuccess()) {
			throw new BaseException("渠道退款失败：" + (result == null ? "无响应" : result.getErrMsg()));
		}
		log.info("refund by channel ok orderNo={} refundNo={} status={}", orderNo, refundNo, result.getStatus());
	}

	/** 查已支付现金订单金额；非已支付抛业务异常。 */
	public BigDecimal requirePaidCashAmount(String orderNo, RechargeProductTypeEnums type) {
		if (type == RechargeProductTypeEnums.MEMBER) {
			MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			if (!MemberPayStatusEnums.isPaid(order.getPayStatus())) {
				throw new BaseException("仅已支付订单可申请退款");
			}
			return order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();
		}
		EpisodeUnlockOrderEntity order = episodeUnlockOrderDao.findByOrderNo(orderNo);
		if (order == null) {
			throw new BaseException(I18nUtil.getMessage("base_data_null"));
		}
		if (!MemberPayStatusEnums.isPaid(order.getPayStatus())) {
			throw new BaseException("仅已支付订单可申请退款");
		}
		return order.getPayAmount() == null ? BigDecimal.ZERO : order.getPayAmount();
	}

	/** 校验订单归属用户。 */
	public void assertOrderOwner(String orderNo, RechargeProductTypeEnums type, Integer uid) {
		if (type == RechargeProductTypeEnums.MEMBER) {
			MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
			if (order == null || !uid.equals(order.getUid())) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			return;
		}
		EpisodeUnlockOrderEntity order = episodeUnlockOrderDao.findByOrderNo(orderNo);
		if (order == null || !uid.equals(order.getUid())) {
			throw new BaseException(I18nUtil.getMessage("base_data_null"));
		}
	}

	private void refundMember(String orderNo, String remark) {
		MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
		if (order == null) {
			throw new BaseException(I18nUtil.getMessage("base_data_null"));
		}
		if (MemberPayStatusEnums.REFUNDED.getIndex().equals(order.getPayStatus())) {
			return;
		}
		if (!MemberPayStatusEnums.isPaid(order.getPayStatus())) {
			throw new BaseException("仅已支付订单可退款");
		}
		// 金币购会员暂不自动回退金币，仅标记退款状态
		MemberOrderEntity upd = new MemberOrderEntity();
		upd.setId(order.getId());
		upd.setPayStatus(MemberPayStatusEnums.REFUNDED.getIndex());
		if (!StringUtils.isEmpty(remark)) {
			upd.setRemark(remark.trim());
		}
		try {
			GenericityUtil.updateDate(upd);
		} catch (Exception e) {
			upd.setGmtModified(new Date());
		}
		memberOrderDao.updateById(upd);
		log.info("member order marked refunded orderNo={}", orderNo);
	}

	private void refundEpisode(String orderNo, String remark) {
		EpisodeUnlockOrderEntity order = episodeUnlockOrderDao.findByOrderNo(orderNo);
		if (order == null) {
			throw new BaseException(I18nUtil.getMessage("base_data_null"));
		}
		if (MemberPayStatusEnums.REFUNDED.getIndex().equals(order.getPayStatus())) {
			return;
		}
		if (!MemberPayStatusEnums.isPaid(order.getPayStatus())) {
			throw new BaseException("仅已支付订单可退款");
		}
		EpisodeUnlockOrderEntity upd = new EpisodeUnlockOrderEntity();
		upd.setId(order.getId());
		upd.setPayStatus(MemberPayStatusEnums.REFUNDED.getIndex());
		upd.setGmtModified(new Date());
		episodeUnlockOrderDao.updateById(upd);
		log.info("episode unlock order marked refunded orderNo={}", orderNo);
	}
}
