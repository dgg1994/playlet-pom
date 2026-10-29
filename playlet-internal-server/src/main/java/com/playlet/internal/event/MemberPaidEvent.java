package com.playlet.internal.event;

import com.playlet.internal.entity.member.MemberOrderEntity;
import org.springframework.context.ApplicationEvent;

/**
 * 会员订单支付成功事件。
 * <p>
 * 由 {@code MemberPaidSupport} 在完成入账后发布，由 {@code MemberAutoRenewSupport} 监听并决定是否开启自动续费。
 * 这样两者只依赖 Spring 事件机制，不形成 Bean 循环依赖。
 */
public class MemberPaidEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final MemberOrderEntity order;

	public MemberPaidEvent(Object source, MemberOrderEntity order) {
		super(source);
		this.order = order;
	}

	public MemberOrderEntity getOrder() {
		return order;
	}
}
