package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.AppVersionConstants;
import com.playlet.oversea.dao.version.AppVersionConfigDao;
import com.playlet.oversea.dao.version.AppVersionI18nDao;
import com.playlet.oversea.entity.version.AppVersionConfigEntity;
import com.playlet.oversea.entity.version.AppVersionI18nEntity;
import com.playlet.oversea.service.VersionManageService;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

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
		// SQL 层分页
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<AppVersionConfigEntity> list = appVersionConfigDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (AppVersionConfigEntity appVersionConfig : list) {
			appVersionConfig.setI18nList(appVersionI18nDao.findByVersionId(appVersionConfig.getId()));
		}
		PageInfo<AppVersionConfigEntity> page = new PageInfo<>(list);
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
			if (entity != null && !StringUtils.isEmpty(entity.getVersionName())) {
				Integer code = toVersionCode(entity.getVersionName());
				if (code == null) {
					return setResultError(I18nUtil.getMessage("version_param_required"));
				}
				entity.setVersionCode(code);
			}
			normalizeDefaults(entity, true);
			GenericityUtil.setDate(entity);
			appVersionConfigDao.insert(entity);
			saveI18nList(entity);
			// 将同类的其他版本停用
			String platform = entity.getPlatform();
			appVersionConfigDao.updateStatusByPlatformAndChannel(AppVersionConstants.STATUS_DISABLE,platform,entity.getVersionName());
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
			} else {
				Integer code = toVersionCode(entity.getVersionName());
				if (code == null) {
					return setResultError(I18nUtil.getMessage("version_param_required"));
				}
				entity.setVersionCode(code);
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

	/**
	 * 验证新增数据
	 *
	 * @param entity
	 * @param creating
	 * @return
	 */
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



	/**
	 * versionName → versionCode，如 1.3.2 => 10302（major*10000 + minor*100 + patch）
	 */
	private Integer toVersionCode(String versionName) {
		if (StringUtils.isEmpty(versionName)) {
			return null;
		}
		String[] parts = versionName.trim().split("\\.");
		if (parts.length < 1 || parts.length > 3) {
			return null;
		}
		try {
			int major = Integer.parseInt(parts[0].trim());
			int minor = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
			int patch = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : 0;
			if (major < 0 || minor < 0 || patch < 0 || minor > 99 || patch > 99) {
				return null;
			}
			return major * 10000 + minor * 100 + patch;
		} catch (NumberFormatException e) {
			return null;
		}
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
