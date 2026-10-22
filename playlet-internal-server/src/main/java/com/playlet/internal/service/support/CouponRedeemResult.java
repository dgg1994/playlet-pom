package com.playlet.internal.service.support;

import com.playlet.internal.entity.coupon.UserCouponEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 优惠券试算/校验结果。
 */
@Data
public class CouponRedeemResult {

	/** 未用券时为 null */
	private UserCouponEntity coupon;

	/** 原价 */
	private BigDecimal originalAmount;

	/** 减免额（元或金币，与币种一致） */
	private BigDecimal discountAmt;

	/** 应付（元或金币） */
	private BigDecimal payAmount;

	/** 金币应付（币种为 COIN 时） */
	private Integer payCoin;

	public boolean hasCoupon() {
		return coupon != null && coupon.getId() != null;
	}
}
