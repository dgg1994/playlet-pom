package com.playlet.internal.service.support;

import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.entity.member.EpisodeUnlockOrderEntity;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.enums.MemberPayStatusEnums;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单退款标记（暂不对接渠道原路退回；会员/单集解锁共用）。
 */
@Slf4j
@Component
public class OrderRefundSupport {

	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;

	/** 按商品类型标记已退款；幂等（已退款直接返回）。 */
	public void markRefunded(String orderNo, RechargeProductTypeEnums type, String remark) {
		if (type == RechargeProductTypeEnums.MEMBER) {
			refundMember(orderNo, remark);
		} else {
			refundEpisode(orderNo, remark);
		}
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
