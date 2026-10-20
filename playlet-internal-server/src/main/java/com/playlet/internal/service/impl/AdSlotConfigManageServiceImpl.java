package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.ad.AdSlotConfigDao;
import com.playlet.internal.entity.ad.AdSlotConfigEntity;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.AdSlotConfigManageService;
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

import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端广告位配置 CRUD。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class AdSlotConfigManageServiceImpl implements AdSlotConfigManageService {

	@Autowired
	private AdSlotConfigDao adSlotConfigDao;

	@Override
	@SysLogAnnotation(module = "广告位配置", type = "POST", remark = "广告位列表")
	public ResponseBase findList(@RequestBody(required = false) AdSlotConfigEntity entity) {
		if (entity == null) {
			entity = new AdSlotConfigEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<AdSlotConfigEntity> list = adSlotConfigDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "广告位配置", type = "POST", remark = "新增广告位")
	public ResponseBase save(@RequestBody AdSlotConfigEntity entity) {
		try {
			String err = validate(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			normalize(entity);
			if (entity.getStatus() == null) {
				entity.setStatus(PublicEnums.ONE.getIndex());
			}
			GenericityUtil.setDate(entity);
			try {
				adSlotConfigDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError("slotCode 已存在");
			}
			log.info("ad slot save id={} code={}", entity.getId(), entity.getSlotCode());
			return setResultSuccess(entity.getId(), I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("ad slot save biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("ad slot save failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "广告位配置", type = "POST", remark = "编辑广告位")
	public ResponseBase update(@RequestBody AdSlotConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AdSlotConfigEntity old = adSlotConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validate(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			normalize(entity);
			// slot_code 唯一：改码时防冲突
			if (!StringUtils.isEmpty(entity.getSlotCode())
					&& !entity.getSlotCode().equals(old.getSlotCode())) {
				AdSlotConfigEntity exist = adSlotConfigDao.findBySlotCode(entity.getSlotCode());
				if (exist != null && !exist.getId().equals(old.getId())) {
					return setResultError("slotCode 已存在");
				}
			}
			GenericityUtil.updateDate(entity);
			adSlotConfigDao.updateById(entity);
			log.info("ad slot update id={}", entity.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("ad slot update biz error id={}", entity == null ? null : entity.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("ad slot update failed id={}", entity == null ? null : entity.getId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "广告位配置", type = "POST", remark = "启用停用")
	public ResponseBase changeStatus(@RequestBody AdSlotConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			boolean validStatus = PublicEnums.ZERO.getIndex().equals(entity.getStatus())
					|| PublicEnums.ONE.getIndex().equals(entity.getStatus());
			if (!validStatus) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AdSlotConfigEntity old = adSlotConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			AdSlotConfigEntity upd = new AdSlotConfigEntity();
			upd.setId(old.getId());
			upd.setStatus(entity.getStatus());
			GenericityUtil.updateDate(upd);
			adSlotConfigDao.updateById(upd);
			log.info("ad slot changeStatus id={} status={}", old.getId(), entity.getStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("ad slot changeStatus biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("ad slot changeStatus failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	private void normalize(AdSlotConfigEntity entity) {
		if (entity.getSlotCode() != null) {
			entity.setSlotCode(entity.getSlotCode().trim());
		}
		if (entity.getSlotName() != null) {
			entity.setSlotName(entity.getSlotName().trim());
		}
		if (entity.getPlatform() != null) {
			entity.setPlatform(entity.getPlatform().trim());
		}
		if (entity.getPlatformSlot() != null) {
			entity.setPlatformSlot(entity.getPlatformSlot().trim());
		}
	}

	private String validate(AdSlotConfigEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && StringUtils.isEmpty(entity.getSlotCode())) {
			return "slotCode 不能为空";
		}
		if (creating && StringUtils.isEmpty(entity.getSlotName())) {
			return "slotName 不能为空";
		}
		if (creating && StringUtils.isEmpty(entity.getPlatform())) {
			return "platform 不能为空";
		}
		if (creating && StringUtils.isEmpty(entity.getPlatformSlot())) {
			return "platformSlot 不能为空";
		}
		if (entity.getStatus() != null
				&& !PublicEnums.ZERO.getIndex().equals(entity.getStatus())
				&& !PublicEnums.ONE.getIndex().equals(entity.getStatus())) {
			return "status 仅支持 0/1";
		}
		return null;
	}
}
