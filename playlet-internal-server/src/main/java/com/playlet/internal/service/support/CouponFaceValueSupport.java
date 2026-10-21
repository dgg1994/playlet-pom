package com.playlet.internal.service.support;

import com.playlet.internal.entity.coupon.CouponTemplateEntity;
import com.playlet.internal.entity.coupon.UserCouponEntity;
import com.playlet.internal.enums.CouponCurrencyEnums;
import com.playlet.internal.enums.CouponTypeEnums;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 优惠券面额展示文案（对齐管理端「面额」列）。
 */
@Component
public class CouponFaceValueSupport {

	public String format(CouponTemplateEntity t) {
		if (t == null) {
			return "-";
		}
		return format(t.getCouponType(), t.getDiscountRate(), t.getThresholdAmt(), t.getReduceAmt(), t.getCurrency());
	}

	public String format(UserCouponEntity c) {
		if (c == null) {
			return "-";
		}
		return format(c.getCouponType(), c.getDiscountRate(), c.getThresholdAmt(), c.getReduceAmt(), c.getCurrency());
	}

	public String format(String couponType, BigDecimal discountRate, BigDecimal thresholdAmt,
			BigDecimal reduceAmt, String currency) {
		CouponTypeEnums type = CouponTypeEnums.ofCode(couponType);
		if (type == null) {
			return "-";
		}
		String unit = unitSuffix(currency);
		switch (type) {
			case PERCENT:
				return toDiscountText(discountRate);
			case FULL_REDUCE:
				return "满" + strip(thresholdAmt) + unit + "减" + strip(reduceAmt) + unit;
			case FIXED:
				return "立减" + strip(reduceAmt) + unit;
			default:
				return "-";
		}
	}

	private static String toDiscountText(BigDecimal rate) {
		if (rate == null) {
			return "-";
		}
		// 0.8 → 8折；0.85 → 8.5折
		BigDecimal zhe = rate.multiply(BigDecimal.TEN).stripTrailingZeros();
		return zhe.toPlainString() + "折";
	}

	private static String unitSuffix(String currency) {
		CouponCurrencyEnums cur = CouponCurrencyEnums.ofCode(currency);
		if (cur == CouponCurrencyEnums.COIN) {
			return "金币";
		}
		return "元";
	}

	private static String strip(BigDecimal amt) {
		if (amt == null) {
			return "0";
		}
		return amt.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
	}
}
