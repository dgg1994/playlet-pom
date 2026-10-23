package com.playlet.internal.service.support;

import com.playlet.internal.api.request.RefundApplyCreateQuery;
import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.dao.member.RefundApplyDao;
import com.playlet.internal.entity.member.EpisodeUnlockOrderEntity;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.entity.member.RefundApplyEntity;
import com.playlet.internal.enums.RefundApplyStatusEnums;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.OrderCodeFactory;
import com.playlet.internal.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 退款申请单组装与校验。
 */
@Component
public class RefundApplySupport {

	@Autowired
	private RefundApplyDao refundApplyDao;
	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;
	@Autowired
	private OrderRefundSupport orderRefundSupport;

	/**
	 * 校验订单并组装待处理申请；uidOverride 非空时校验归属（C端），空则从订单取 uid（管理端代提）。
	 */
	public RefundApplyEntity buildApplyFromOrder(RefundApplyCreateQuery query, Integer uidOverride) {
		if (query == null || StringUtils.isEmpty(query.getOrderNo())
				|| StringUtils.isEmpty(query.getProductType())
				|| StringUtils.isEmpty(query.getRefundReason())) {
			throw new BaseException(I18nUtil.getMessage("param_not_null"));
		}
		RechargeProductTypeEnums type = RechargeProductTypeEnums.ofCode(query.getProductType());
		if (type == null) {
			throw new BaseException("productType 须为 MEMBER / EPISODE");
		}
		String orderNo = query.getOrderNo().trim();
		String reason = query.getRefundReason().trim();
		if (reason.length() > 128) {
			throw new BaseException("退款原因过长");
		}
		if (refundApplyDao.findPendingByOrderNo(orderNo) != null) {
			throw new BaseException("该订单已有待处理退款申请");
		}
		Integer uid;
		BigDecimal amount;
		if (type == RechargeProductTypeEnums.MEMBER) {
			MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			uid = order.getUid();
			amount = orderRefundSupport.requirePaidCashAmount(orderNo, type);
		} else {
			EpisodeUnlockOrderEntity order = episodeUnlockOrderDao.findByOrderNo(orderNo);
			if (order == null) {
				throw new BaseException(I18nUtil.getMessage("base_data_null"));
			}
			uid = order.getUid();
			amount = orderRefundSupport.requirePaidCashAmount(orderNo, type);
		}
		if (uidOverride != null) {
			orderRefundSupport.assertOrderOwner(orderNo, type, uidOverride);
			uid = uidOverride;
		}
		RefundApplyEntity apply = new RefundApplyEntity();
		apply.setApplyNo(OrderCodeFactory.getRefundCode(uid == null ? 0L : uid.longValue()));
		apply.setOrderNo(orderNo);
		apply.setProductType(type.getCode());
		apply.setUid(uid);
		apply.setRefundAmount(amount);
		apply.setRefundReason(reason);
		apply.setStatus(RefundApplyStatusEnums.PENDING.getIndex());
		return apply;
	}
}
