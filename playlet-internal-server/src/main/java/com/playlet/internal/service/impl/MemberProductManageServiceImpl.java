package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.member.MemberProductDao;
import com.playlet.internal.entity.member.MemberProductEntity;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.MemberProductManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端会员套餐 CRUD。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class MemberProductManageServiceImpl implements MemberProductManageService {

	@Autowired
	private MemberProductDao memberProductDao;

	@Override
	@SysLogAnnotation(module = "会员套餐", type = "POST", remark = "套餐列表")
	public ResponseBase findList(@RequestBody(required = false) MemberProductEntity entity) {
		if (entity == null) {
			entity = new MemberProductEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<MemberProductEntity> list = memberProductDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "会员套餐", type = "POST", remark = "新增套餐")
	public ResponseBase save(@RequestBody MemberProductEntity entity) {
		try {
			String err = validate(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			normalize(entity);
			if (entity.getStatus() == null) {
				entity.setStatus(PublicEnums.ONE.getIndex());
			}
			if (entity.getSortWeight() == null) {
				entity.setSortWeight(0);
			}
			if (entity.getPriceCoin() == null) {
				entity.setPriceCoin(0);
			}
			// 未填原价时默认与售价一致，列表仍可展示划线价
			if (entity.getOriginalPrice() == null && entity.getPriceYuan() != null) {
				entity.setOriginalPrice(entity.getPriceYuan());
			}
			GenericityUtil.setDate(entity);
			try {
				memberProductDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError("套餐编码已存在");
			}
			log.info("member product save id={} code={}", entity.getId(), entity.getProductCode());
			return setResultSuccess(entity.getId(), I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("member product save biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("member product save failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "会员套餐", type = "POST", remark = "编辑套餐")
	public ResponseBase update(@RequestBody MemberProductEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			MemberProductEntity old = memberProductDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validate(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			normalize(entity);
			GenericityUtil.updateDate(entity);
			memberProductDao.updateById(entity);
			log.info("member product update id={}", entity.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("member product update biz error id={}", entity == null ? null : entity.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("member product update failed id={}", entity == null ? null : entity.getId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "会员套餐", type = "POST", remark = "上架下架")
	public ResponseBase changeStatus(@RequestBody MemberProductEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			boolean validStatus = PublicEnums.ZERO.getIndex().equals(entity.getStatus())
					|| PublicEnums.ONE.getIndex().equals(entity.getStatus());
			if (!validStatus) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			MemberProductEntity old = memberProductDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			MemberProductEntity upd = new MemberProductEntity();
			upd.setId(old.getId());
			upd.setStatus(entity.getStatus());
			GenericityUtil.updateDate(upd);
			memberProductDao.updateById(upd);
			log.info("member product changeStatus id={} status={}", old.getId(), entity.getStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("member product changeStatus biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("member product changeStatus failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	private void normalize(MemberProductEntity entity) {
		if (entity.getProductCode() != null) {
			entity.setProductCode(entity.getProductCode().trim());
		}
		if (entity.getProductName() != null) {
			entity.setProductName(entity.getProductName().trim());
		}
	}

	private String validate(MemberProductEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && StringUtils.isEmpty(entity.getProductCode())) {
			return "productCode 不能为空";
		}
		if (creating && StringUtils.isEmpty(entity.getProductName())) {
			return "productName 不能为空";
		}
		if (creating && (entity.getDurationDays() == null || entity.getDurationDays() < 1)) {
			return "durationDays 须 >= 1";
		}
		if (entity.getDurationDays() != null && entity.getDurationDays() < 1) {
			return "durationDays 须 >= 1";
		}
		if (entity.getPriceYuan() != null && entity.getPriceYuan().compareTo(BigDecimal.ZERO) < 0) {
			return "priceYuan 不能为负";
		}
		if (entity.getOriginalPrice() != null && entity.getOriginalPrice().compareTo(BigDecimal.ZERO) < 0) {
			return "originalPrice 不能为负";
		}
		// 有原价且有售价时，原价须 >= 售价
		boolean bothPricePresent = entity.getOriginalPrice() != null && entity.getPriceYuan() != null;
		if (bothPricePresent && entity.getOriginalPrice().compareTo(entity.getPriceYuan()) < 0) {
			return "originalPrice 不能小于 priceYuan";
		}
		if (entity.getPriceCoin() != null && entity.getPriceCoin() < 0) {
			return "priceCoin 不能为负";
		}
		if (entity.getStatus() != null
				&& !PublicEnums.ZERO.getIndex().equals(entity.getStatus())
				&& !PublicEnums.ONE.getIndex().equals(entity.getStatus())) {
			return "status 仅支持 0/1";
		}
		return null;
	}
}
