package com.playlet.internal.service.support;

import com.playlet.internal.api.request.EpisodeUnlockCashNotifyReqEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.member.EpisodeUnlockOrderDao;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.enums.RechargeProductTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.EpisodeUnlockApiService;
import com.playlet.internal.service.support.member.MemberPaidSupport;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单置为已支付：现金订单成员套餐与单集解锁共用入口，复用各自原有的开通逻辑（幂等）。
 */
@Slf4j
@Component
public class OrderPaidSupport {

	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private EpisodeUnlockOrderDao episodeUnlockOrderDao;
	@Autowired
	private MemberPaidSupport memberPaidSupport;
	@Autowired
	private EpisodeUnlockApiService episodeUnlockApiService;

	/** 订单归属的商品类型；两表都没有返回 null */
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

	/** 标记已支付并开通权益；幂等，失败抛业务异常 */
	public void markPaid(String orderNo, String thirdTradeNo) {
		RechargeProductTypeEnums type = resolveType(orderNo);
		if (type == null) {
			throw new BaseException("订单不存在");
		}
		if (type == RechargeProductTypeEnums.MEMBER) {
			// 会员入账走 MemberPaidSupport（不再回调 MemberApiService，避免形成 Bean 循环依赖）
			try {
				memberPaidSupport.markPaid(orderNo, thirdTradeNo);
			} catch (Exception e) {
				throw new BaseException("会员支付确认失败", e);
			}
			log.info("order mark paid ok orderNo={} thirdTradeNo={} type={}", orderNo, thirdTradeNo, type.getCode());
			return;
		}
		EpisodeUnlockCashNotifyReqEntity req = new EpisodeUnlockCashNotifyReqEntity();
		req.setOrderNo(orderNo);
		req.setThirdTradeNo(thirdTradeNo);
		ResponseBase resp = episodeUnlockApiService.cashNotify(req);
		if (resp == null || !Integer.valueOf(Constants.HTTP_RES_CODE_200).equals(resp.getCode())) {
			throw new BaseException(resp == null || StringUtils.isEmpty(resp.getMsg())
					? "支付确认失败" : resp.getMsg());
		}
		log.info("order mark paid ok orderNo={} thirdTradeNo={} type={}", orderNo, thirdTradeNo, type.getCode());
	}
}
