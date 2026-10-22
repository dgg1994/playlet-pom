package com.playlet.internal.service.support;

import com.playlet.internal.dao.coupon.CouponTemplateDao;
import com.playlet.internal.dao.coupon.UserCouponDao;
import com.playlet.internal.entity.coupon.UserCouponEntity;
import com.playlet.internal.enums.CouponCurrencyEnums;
import com.playlet.internal.enums.CouponScopeEnums;
import com.playlet.internal.enums.CouponTypeEnums;
import com.playlet.internal.enums.UserCouponStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 优惠券校验、算价与核销（会员 / 集解锁等下单复用）。
 */
@Slf4j
@Component
public class CouponRedeemSupport {

	@Autowired
	private UserCouponDao userCouponDao;
	@Autowired
	private CouponTemplateDao couponTemplateDao;

	/**
	 * 试算：userCouponId 为空则无优惠；否则校验归属/状态/范围/币种并算应付。
	 *
	 * @param requiredScope MEMBER / COIN_CONSUME / RECHARGE
	 * @param currency      YUAN / COIN
	 * @param originalAmt   原价（元或金币数量）
	 */
	public CouponRedeemResult resolve(Integer uid, Integer userCouponId, String requiredScope,
			CouponCurrencyEnums currency, BigDecimal originalAmt) {
		CouponRedeemResult result = new CouponRedeemResult();
		if (originalAmt == null || originalAmt.compareTo(BigDecimal.ZERO) < 0) {
			throw new BaseException("订单金额无效");
		}
		result.setOriginalAmount(originalAmt);
		result.setDiscountAmt(BigDecimal.ZERO);
		result.setPayAmount(originalAmt);
		if (CouponCurrencyEnums.COIN == currency) {
			result.setPayCoin(toCoinInt(originalAmt));
		}
		if (userCouponId == null) {
			return result;
		}
		if (uid == null) {
			throw new BaseException("请登录");
		}
		UserCouponEntity coupon = userCouponDao.findByIdAndUid(userCouponId, uid);
		if (coupon == null) {
			throw new BaseException("优惠券不存在");
		}
		assertUsable(coupon, requiredScope, currency, originalAmt);
		applyDiscount(result, coupon, originalAmt, currency);
		result.setCoupon(coupon);
		return result;
	}

	/** 列出当前场景可用券（含本单试算减免）。 */
	public List<CouponRedeemResult> listAvailable(Integer uid, String requiredScope,
			CouponCurrencyEnums currency, BigDecimal originalAmt) {
		List<CouponRedeemResult> out = new ArrayList<>();
		if (uid == null || currency == null || originalAmt == null
				|| originalAmt.compareTo(BigDecimal.ZERO) < 0) {
			return out;
		}
		List<UserCouponEntity> list = userCouponDao.findByUid(uid, UserCouponStatusEnums.UNUSED.getIndex());
		if (list == null || list.isEmpty()) {
			return out;
		}
		Date now = new Date();
		for (UserCouponEntity row : list) {
			if (row.getExpireTime() != null && row.getExpireTime().before(now)) {
				continue;
			}
			try {
				assertUsable(row, requiredScope, currency, originalAmt);
			} catch (BaseException e) {
				continue;
			}
			CouponRedeemResult one = new CouponRedeemResult();
			one.setOriginalAmount(originalAmt);
			applyDiscount(one, row, originalAmt, currency);
			one.setCoupon(row);
			out.add(one);
		}
		return out;
	}

	/**
	 * 支付成功后核销；幂等：已对本订单核销则直接返回。templateId 可空，空则从持券读取。
	 */
	public void confirmUsed(Integer uid, Integer userCouponId, Integer templateId, String orderNo) {
		if (userCouponId == null) {
			return;
		}
		if (uid == null || StringUtils.isEmpty(orderNo)) {
			throw new BaseException("优惠券核销参数错误");
		}
		UserCouponEntity coupon = userCouponDao.findByIdAndUid(userCouponId, uid);
		Integer tid = templateId;
		if (tid == null && coupon != null) {
			tid = coupon.getTemplateId();
		}
		int rows = userCouponDao.markUsed(userCouponId, uid, orderNo);
		if (rows > 0) {
			if (tid != null) {
				couponTemplateDao.incrUsedCount(tid);
			}
			log.info("coupon markUsed ok uid={} couponId={} orderNo={}", uid, userCouponId, orderNo);
			return;
		}
		boolean sameOrder = coupon != null
				&& UserCouponStatusEnums.USED.getIndex().equals(coupon.getStatus())
				&& orderNo.equals(coupon.getUsedOrderNo());
		if (sameOrder) {
			return;
		}
		throw new BaseException("优惠券核销失败");
	}

	private void assertUsable(UserCouponEntity coupon, String requiredScope,
			CouponCurrencyEnums currency, BigDecimal originalAmt) {
		if (!UserCouponStatusEnums.UNUSED.getIndex().equals(coupon.getStatus())) {
			throw new BaseException("优惠券不可用");
		}
		if (coupon.getExpireTime() != null && coupon.getExpireTime().before(new Date())) {
			throw new BaseException("优惠券已过期");
		}
		if (!scopeMatch(coupon.getScope(), requiredScope)) {
			throw new BaseException("优惠券不适用于当前场景");
		}
		CouponCurrencyEnums couponCur = CouponCurrencyEnums.ofCode(coupon.getCurrency());
		if (couponCur == null || currency == null || couponCur != currency) {
			throw new BaseException("优惠券币种不匹配");
		}
		CouponTypeEnums type = CouponTypeEnums.ofCode(coupon.getCouponType());
		if (type == null) {
			throw new BaseException("优惠券类型无效");
		}
		if (type == CouponTypeEnums.FULL_REDUCE) {
			BigDecimal threshold = coupon.getThresholdAmt();
			if (threshold == null || originalAmt.compareTo(threshold) < 0) {
				throw new BaseException("未满优惠券使用门槛");
			}
		}
	}

	private static boolean scopeMatch(String couponScope, String requiredScope) {
		if (StringUtils.isEmpty(couponScope) || StringUtils.isEmpty(requiredScope)) {
			return false;
		}
		if (CouponScopeEnums.ALL.getCode().equalsIgnoreCase(couponScope)) {
			return true;
		}
		return couponScope.equalsIgnoreCase(requiredScope.trim());
	}

	private void applyDiscount(CouponRedeemResult result, UserCouponEntity coupon,
			BigDecimal originalAmt, CouponCurrencyEnums currency) {
		CouponTypeEnums type = CouponTypeEnums.ofCode(coupon.getCouponType());
		BigDecimal discount;
		BigDecimal pay;
		if (type == CouponTypeEnums.PERCENT) {
			BigDecimal rate = coupon.getDiscountRate();
			if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
				throw new BaseException("优惠券折扣率无效");
			}
			pay = originalAmt.multiply(rate).setScale(2, RoundingMode.HALF_UP);
			discount = originalAmt.subtract(pay);
		} else if (type == CouponTypeEnums.FULL_REDUCE || type == CouponTypeEnums.FIXED) {
			BigDecimal reduce = coupon.getReduceAmt() == null ? BigDecimal.ZERO : coupon.getReduceAmt();
			if (reduce.compareTo(BigDecimal.ZERO) <= 0) {
				throw new BaseException("优惠券减免额无效");
			}
			discount = reduce.min(originalAmt);
			pay = originalAmt.subtract(discount);
		} else {
			throw new BaseException("优惠券类型无效");
		}
		if (pay.compareTo(BigDecimal.ZERO) < 0) {
			pay = BigDecimal.ZERO;
			discount = originalAmt;
		}
		result.setDiscountAmt(discount);
		result.setPayAmount(pay);
		if (CouponCurrencyEnums.COIN == currency) {
			// 金币按整数：应付向上取整避免少付；减免=原价-应付
			int origCoin = toCoinInt(originalAmt);
			int payCoin = pay.setScale(0, RoundingMode.CEILING).intValue();
			if (payCoin < 0) {
				payCoin = 0;
			}
			if (payCoin > origCoin) {
				payCoin = origCoin;
			}
			result.setPayCoin(payCoin);
			result.setPayAmount(BigDecimal.valueOf(payCoin));
			result.setDiscountAmt(BigDecimal.valueOf(origCoin - payCoin));
		}
	}

	private static int toCoinInt(BigDecimal amt) {
		return amt.setScale(0, RoundingMode.HALF_UP).intValue();
	}
}
