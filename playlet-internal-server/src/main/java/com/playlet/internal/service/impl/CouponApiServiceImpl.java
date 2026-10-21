package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.UserCouponItemEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.coupon.UserCouponDao;
import com.playlet.internal.entity.coupon.UserCouponEntity;
import com.playlet.internal.enums.UserCouponStatusEnums;
import com.playlet.internal.service.CouponApiService;
import com.playlet.internal.service.support.CouponFaceValueSupport;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * C 端我的优惠券。
 */
@Slf4j
@RestController
@CrossOrigin
public class CouponApiServiceImpl implements CouponApiService {

	@Autowired
	private UserCouponDao userCouponDao;
	@Autowired
	private CouponFaceValueSupport couponFaceValueSupport;

	@Override
	public ResponseBase myList(HttpServletRequest request, Integer status) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (status != null && !UserCouponStatusEnums.isValid(status)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		List<UserCouponEntity> list = userCouponDao.findByUid(uid, status);
		if (list == null) {
			list = new ArrayList<>();
		}
		Date now = new Date();
		List<UserCouponItemEntity> result = new ArrayList<>(list.size());
		for (UserCouponEntity row : list) {
			// 未用且已过期：惰性标记为过期（不强制事务，失败不影响列表）
			if (UserCouponStatusEnums.UNUSED.getIndex().equals(row.getStatus())
					&& row.getExpireTime() != null
					&& row.getExpireTime().before(now)) {
				try {
					UserCouponEntity upd = new UserCouponEntity();
					upd.setId(row.getId());
					upd.setStatus(UserCouponStatusEnums.EXPIRED.getIndex());
					GenericityUtil.updateDate(upd);
					userCouponDao.updateById(upd);
					row.setStatus(UserCouponStatusEnums.EXPIRED.getIndex());
				} catch (Exception e) {
					log.warn("mark coupon expired failed id={}", row.getId());
				}
				if (status != null && !UserCouponStatusEnums.EXPIRED.getIndex().equals(status)) {
					continue;
				}
			}
			result.add(toItem(row));
		}
		return setResultSuccess(result, I18nUtil.getMessage("base_success"));
	}

	private UserCouponItemEntity toItem(UserCouponEntity row) {
		UserCouponItemEntity item = new UserCouponItemEntity();
		item.setId(row.getId());
		item.setTemplateId(row.getTemplateId());
		item.setCouponCode(row.getCouponCode());
		item.setCouponName(row.getCouponName());
		item.setCouponType(row.getCouponType());
		item.setFaceValueText(couponFaceValueSupport.format(row));
		item.setCurrency(row.getCurrency());
		item.setScope(row.getScope());
		item.setDiscountRate(row.getDiscountRate());
		item.setThresholdAmt(row.getThresholdAmt());
		item.setReduceAmt(row.getReduceAmt());
		item.setStatus(row.getStatus());
		item.setExpireTime(row.getExpireTime());
		item.setSetTime(row.getSetTime());
		return item;
	}
}
