package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.AppVersionConstants;
import com.playlet.internal.dao.version.AppVersionConfigDao;
import com.playlet.internal.dao.version.AppVersionI18nDao;
import com.playlet.internal.entity.version.AppVersionConfigEntity;
import com.playlet.internal.entity.version.AppVersionI18nEntity;
import com.playlet.internal.enums.AppVersionChannelEnums;
import com.playlet.internal.enums.AppVersionPlatformEnums;
import com.playlet.internal.service.VersionManageService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class VersionManageServiceImpl implements VersionManageService {

	@Autowired
	private AppVersionConfigDao appVersionConfigDao;
	@Autowired
	private AppVersionI18nDao appVersionI18nDao;

	@Override
	@SysLogAnnotation(module = "版本管理", type = "POST", remark = "版本列表")
	public ResponseBase findList(@RequestBody AppVersionConfigEntity entity) {
		if (entity == null) {
			entity = new AppVersionConfigEntity();
		}
		if (StringUtils.isEmpty(entity.getLangue())) {
			entity.setLangue(LanguageContext.getLanguage());
		}
		List<AppVersionConfigEntity> list = appVersionConfigDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		String language = LanguageContext.getLanguage();
		for (AppVersionConfigEntity row : list) {
			if (StringUtils.isEmpty(row.getTitle())) {
				row.setTitle(appVersionI18nDao.selectTitleByVersionId(row.getId(), language));
			}
		}
		List<AppVersionConfigEntity> pageList = GenericityUtil.Page(list, entity.getPageNumber(), entity.getPageSize());
		PageInfo<AppVersionConfigEntity> page = new PageInfo<>(pageList);
		page.setTotal(list.size());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "版本管理", type = "POST", remark = "版本详情")
	public ResponseBase detail(@RequestBody AppVersionConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppVersionConfigEntity row = appVersionConfigDao.selectById(entity.getId());
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		row.setI18nList(appVersionI18nDao.findByVersionId(row.getId()));
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "版本管理", type = "POST", remark = "新增版本")
	public ResponseBase save(@RequestBody AppVersionConfigEntity entity) {
		try {
			String err = validateSave(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			normalizeDefaults(entity, true);
			GenericityUtil.setDate(entity);
			try {
				appVersionConfigDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError(I18nUtil.getMessage("version_code_exist"));
			}
			saveI18nList(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "版本管理", type = "POST", remark = "编辑版本")
	public ResponseBase update(@RequestBody AppVersionConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppVersionConfigEntity old = appVersionConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateSave(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			if (StringUtils.isEmpty(entity.getPlatform())) {
				entity.setPlatform(old.getPlatform());
			}
			if (StringUtils.isEmpty(entity.getChannel())) {
				entity.setChannel(old.getChannel());
			}
			if (entity.getVersionCode() == null) {
				entity.setVersionCode(old.getVersionCode());
			}
			if (StringUtils.isEmpty(entity.getVersionName())) {
				entity.setVersionName(old.getVersionName());
			}
			normalizeDefaults(entity, false);
			AppVersionConfigEntity conflict = appVersionConfigDao.findByUnique(
					entity.getPlatform(), entity.getChannel(), entity.getVersionCode());
			if (conflict != null && !conflict.getId().equals(old.getId())) {
				return setResultError(I18nUtil.getMessage("version_code_exist"));
			}
			GenericityUtil.updateDate(entity);
			appVersionConfigDao.updateById(entity);
			if (entity.getI18nList() != null) {
				if (entity.getI18nList().isEmpty()) {
					return setResultError(I18nUtil.getMessage("version_i18n_required"));
				}
				String i18nErr = validateI18nList(entity.getI18nList());
				if (i18nErr != null) {
					return setResultError(i18nErr);
				}
				appVersionI18nDao.deleteByVersionId(old.getId());
				saveI18nList(entity);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "版本管理", type = "POST", remark = "启停版本")
	public ResponseBase changeStatus(@RequestBody AppVersionConfigEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (entity.getStatus() != 0 && entity.getStatus() != 1) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppVersionConfigEntity old = appVersionConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		AppVersionConfigEntity upd = new AppVersionConfigEntity();
		upd.setId(old.getId());
		upd.setStatus(entity.getStatus());
		try {
			GenericityUtil.updateDate(upd);
			appVersionConfigDao.updateById(upd);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "版本管理", type = "POST", remark = "删除版本")
	public ResponseBase delete(@RequestBody AppVersionConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			AppVersionConfigEntity old = appVersionConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			appVersionI18nDao.deleteByVersionId(old.getId());
			appVersionConfigDao.deleteById(old.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void normalizeDefaults(AppVersionConfigEntity entity, boolean creating) {
		entity.setPlatform(entity.getPlatform().trim().toLowerCase());
		if (StringUtils.isEmpty(entity.getChannel())) {
			entity.setChannel(AppVersionConstants.DEFAULT_CHANNEL);
		} else {
			entity.setChannel(entity.getChannel().trim().toLowerCase());
		}
		entity.setVersionName(entity.getVersionName().trim());
		if (entity.getIsForce() == null) {
			entity.setIsForce(0);
		}
		if (creating && entity.getStatus() == null) {
			entity.setStatus(0);
		}
	}

	private String validateSave(AppVersionConfigEntity request, boolean creating) {
		if (request == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating) {
			if (StringUtils.isEmpty(request.getPlatform()) || request.getVersionCode() == null
					|| StringUtils.isEmpty(request.getVersionName())) {
				return I18nUtil.getMessage("version_param_required");
			}
			if (request.getI18nList() == null || request.getI18nList().isEmpty()) {
				return I18nUtil.getMessage("version_i18n_required");
			}
			String i18nErr = validateI18nList(request.getI18nList());
			if (i18nErr != null) {
				return i18nErr;
			}
		} else {
			if (request.getPlatform() != null && StringUtils.isEmpty(request.getPlatform().trim())) {
				return I18nUtil.getMessage("version_param_required");
			}
			if (request.getVersionName() != null && StringUtils.isEmpty(request.getVersionName().trim())) {
				return I18nUtil.getMessage("version_param_required");
			}
		}
		if (request.getPlatform() != null && !AppVersionPlatformEnums.isValid(request.getPlatform())) {
			return I18nUtil.getMessage("version_platform_invalid");
		}
		if (request.getChannel() != null && !StringUtils.isEmpty(request.getChannel().trim())
				&& !AppVersionChannelEnums.isValid(request.getChannel())) {
			return I18nUtil.getMessage("version_channel_invalid");
		}
		if (request.getVersionCode() != null && request.getVersionCode() < 1) {
			return I18nUtil.getMessage("base_error");
		}
		if (request.getIsForce() != null && request.getIsForce() != 0 && request.getIsForce() != 1) {
			return I18nUtil.getMessage("base_error");
		}
		if (request.getStatus() != null && request.getStatus() != 0 && request.getStatus() != 1) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && appVersionConfigDao.findByUnique(
				request.getPlatform().trim().toLowerCase(),
				StringUtils.isEmpty(request.getChannel()) ? AppVersionConstants.DEFAULT_CHANNEL
						: request.getChannel().trim().toLowerCase(),
				request.getVersionCode()) != null) {
			return I18nUtil.getMessage("version_code_exist");
		}
		return null;
	}

	private String validateI18nList(List<AppVersionI18nEntity> i18nList) {
		Set<String> langues = new HashSet<>();
		for (AppVersionI18nEntity i18n : i18nList) {
			if (i18n == null || StringUtils.isEmpty(i18n.getLangue())
					|| StringUtils.isEmpty(i18n.getTitle())
					|| StringUtils.isEmpty(i18n.getTitle().trim())
					|| StringUtils.isEmpty(i18n.getContent())
					|| StringUtils.isEmpty(i18n.getContent().trim())) {
				return I18nUtil.getMessage("version_i18n_required");
			}
			if (!langues.add(i18n.getLangue().trim())) {
				return I18nUtil.getMessage("base_error");
			}
		}
		return null;
	}

	private void saveI18nList(AppVersionConfigEntity entity) throws Exception {
		List<AppVersionI18nEntity> i18nList = entity.getI18nList();
		if (i18nList == null) {
			return;
		}
		for (AppVersionI18nEntity src : i18nList) {
			AppVersionI18nEntity row = new AppVersionI18nEntity();
			row.setVersionId(entity.getId());
			row.setLangue(src.getLangue().trim());
			row.setTitle(src.getTitle().trim());
			row.setContent(src.getContent().trim());
			GenericityUtil.setDate(row);
			appVersionI18nDao.insert(row);
		}
	}
}
