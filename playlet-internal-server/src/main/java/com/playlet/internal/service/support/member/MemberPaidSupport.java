package com.playlet.internal.service.support.member;

import com.playlet.internal.constants.MemberConstants;
import com.playlet.internal.dao.member.MemberOrderDao;
import com.playlet.internal.dao.member.UserMemberDao;
import com.playlet.internal.entity.member.MemberOrderEntity;
import com.playlet.internal.entity.member.UserMemberEntity;
import com.playlet.internal.enums.MemberPayStatusEnums;
import com.playlet.internal.enums.MemberPayTypeEnums;
import com.playlet.internal.event.MemberPaidEvent;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.support.CouponRedeemSupport;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 会员订单入账：现金订单置已支付 + 开通/续期会员 + 核销优惠券。
 * <p>
 * 这是会员支付链路的「最底层组件」，只向下依赖 DAO 与券组件，不被任何业务 Service 反向依赖：
 * <ul>
 *   <li>{@code MemberApiServiceImpl}：C 端支付确认接口</li>
 *   <li>{@code OrderPaidSupport}：三方支付/代扣回调的统一入口</li>
 *   <li>{@code MemberAutoRenewSupport}：自动续费代扣成功后复用入账</li>
 * </ul>
 * 三者都依赖它，它谁也不依赖，从而打破原有的循环依赖。
 * <p>
 * 入账完成后发布 {@link MemberPaidEvent}，由自动续费组件监听处理后续动作（开启自动续费）。
 */
@Slf4j
@Component
public class MemberPaidSupport {

	@Autowired
	private MemberOrderDao memberOrderDao;
	@Autowired
	private UserMemberDao userMemberDao;
	@Autowired
	private CouponRedeemSupport couponRedeemSupport;
	@Autowired
	private ApplicationEventPublisher eventPublisher;

	/**
	 * 现金会员订单置已支付并开通/续期；幂等。
	 *
	 * @return 更新后的订单；已支付过则直接返回库中记录
	 */
	@Transactional(rollbackFor = Exception.class)
	public MemberOrderEntity markPaid(String orderNo, String thirdTradeNo) throws Exception {
		MemberOrderEntity order = memberOrderDao.findByOrderNo(orderNo);
		if (order == null) {
			throw new BaseException(I18nUtil.getMessage("base_data_null"));
		}
		// 幂等：已支付直接返回
		if (MemberPayStatusEnums.isPaid(order.getPayStatus())) {
			return order;
		}
		if (!MemberPayTypeEnums.isCash(order.getPayType())) {
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}
		if (!MemberPayStatusEnums.WAIT.getIndex().equals(order.getPayStatus())) {
			throw new BaseException(I18nUtil.getMessage("base_error"));
		}

		Date[] expireRange = calcExpireRange(order.getUid(), order.getDurationDays());
		Date now = new Date();
		order.setPayStatus(MemberPayStatusEnums.PAID.getIndex());
		order.setPayTime(now);
		order.setThirdTradeNo(StringUtils.isEmpty(thirdTradeNo) ? order.getOrderNo() : thirdTradeNo.trim());
		order.setExpireBefore(expireRange[0]);
		order.setExpireAfter(expireRange[1]);
		order.setGmtModified(now);
		memberOrderDao.updateById(order);
		grantOrRenewMember(order);
		// 入账后发布事件：由 MemberAutoRenewSupport 监听开启自动续费（同步事件，异常随事务回滚）
		eventPublisher.publishEvent(new MemberPaidEvent(this, order));
		// 现金支付成功后核销优惠券（幂等）
		couponRedeemSupport.confirmUsed(order.getUid(), order.getUserCouponId(), null, orderNo);
		log.info("member paid ok uid={} orderNo={} channel={} thirdTradeNo={} expireAfter={}",
				order.getUid(), orderNo, order.getPayChannel(), order.getThirdTradeNo(), order.getExpireAfter());
		return order;
	}

	/** 开通或续期会员：无记录则新增，有记录则直接把到期时间推进到订单的 expireAfter */
	public void grantOrRenewMember(MemberOrderEntity order) throws Exception {
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

	/**
	 * 计算本次开通前后的到期时间：[expireBefore, expireAfter]。
	 * 未到期会员在原到期时间上顺延，已过期/非会员从当前时间起算。
	 */
	public Date[] calcExpireRange(Integer uid, int durationDays) {
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
}
