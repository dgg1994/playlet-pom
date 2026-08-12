package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.medal.MedalConfigDao;
import com.playlet.internal.dao.medal.MedalConfigI18nDao;
import com.playlet.internal.entity.medal.MedalConfigEntity;
import com.playlet.internal.entity.medal.MedalConfigI18nEntity;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.MedalManageService;
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
import java.util.regex.Pattern;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class MedalManageServiceImpl implements MedalManageService {

	private static final Pattern MEDAL_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,63}$");

	@Autowired
	private MedalConfigDao medalConfigDao;
	@Autowired
	private MedalConfigI18nDao medalConfigI18nDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	@SysLogAnnotation(module = "勋章管理", type = "POST", remark = "勋章列表")
	public ResponseBase findList(@RequestBody MedalConfigEntity entity) {
		if (entity == null) {
			entity = new MedalConfigEntity();
		}
		if (StringUtils.isEmpty(entity.getLangue())) {
			entity.setLangue(LanguageContext.getLanguage());
		}
		// SQL 层分页
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<MedalConfigEntity> list = medalConfigDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		String language = LanguageContext.getLanguage();
		for (MedalConfigEntity row : list) {
			if (StringUtils.isEmpty(row.getMedalName())) {
				row.setMedalName(medalConfigI18nDao.selectNameByMedalId(row.getId(), language));
			}
			signIconUrls(row);
		}
		PageInfo<MedalConfigEntity> page = new PageInfo<>(list);
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "勋章管理", type = "POST", remark = "勋章详情")
	public ResponseBase detail(@RequestBody MedalConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		MedalConfigEntity row = medalConfigDao.selectById(entity.getId());
		if (row == null || row.getIsDeleted() != null && row.getIsDeleted() == 1) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		row.setI18nList(medalConfigI18nDao.findByMedalId(row.getId()));
		signIconUrls(row);
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "勋章管理", type = "POST", remark = "新增勋章")
	public ResponseBase save(@RequestBody MedalConfigEntity entity) {
		try {
			String err = validateSave(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			entity.setMedalCode(normalizeMedalCode(entity.getMedalCode()));
			if (entity.getStatus() == null) {
				entity.setStatus(1);
			}
			if (entity.getIsDeleted() == null) {
				entity.setIsDeleted(0);
			}
			if (entity.getSortWeight() == null) {
				entity.setSortWeight(0);
			}
			if (entity.getTargetCount() == null) {
				entity.setTargetCount(1);
			}
			if (entity.getRewardCoin() == null) {
				entity.setRewardCoin(0);
			}
			GenericityUtil.setDate(entity);
			try {
				medalConfigDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError(I18nUtil.getMessage("medal_code_exist"));
			}
			saveI18nList(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "勋章管理", type = "POST", remark = "编辑勋章")
	public ResponseBase update(@RequestBody MedalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			MedalConfigEntity old = medalConfigDao.selectById(entity.getId());
			if (old == null || old.getIsDeleted() != null && old.getIsDeleted() == 1) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateSave(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			if (entity.getMedalCode() != null
					&& !normalizeMedalCode(entity.getMedalCode()).equals(old.getMedalCode())) {
				return setResultError(I18nUtil.getMessage("medal_code_immutable"));
			}
			entity.setMedalCode(old.getMedalCode());
			GenericityUtil.updateDate(entity);
			medalConfigDao.updateById(entity);
			if (entity.getI18nList() != null) {
				if (entity.getI18nList().isEmpty()) {
					return setResultError(I18nUtil.getMessage("medal_i18n_required"));
				}
				String i18nErr = validateI18nList(entity.getI18nList());
				if (i18nErr != null) {
					return setResultError(i18nErr);
				}
				medalConfigI18nDao.deleteByMedalId(old.getId());
				saveI18nList(entity);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "勋章管理", type = "POST", remark = "启停勋章")
	public ResponseBase changeStatus(@RequestBody MedalConfigEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (entity.getStatus() != 0 && entity.getStatus() != 1) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		MedalConfigEntity old = medalConfigDao.selectById(entity.getId());
		if (old == null || old.getIsDeleted() != null && old.getIsDeleted() == 1) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		MedalConfigEntity upd = new MedalConfigEntity();
		upd.setId(old.getId());
		upd.setStatus(entity.getStatus());
		try {
			GenericityUtil.updateDate(upd);
			medalConfigDao.updateById(upd);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "勋章管理", type = "POST", remark = "删除勋章")
	public ResponseBase delete(@RequestBody MedalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			MedalConfigEntity old = medalConfigDao.selectById(entity.getId());
			if (old == null || old.getIsDeleted() != null && old.getIsDeleted() == 1) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			medalConfigDao.softDelete(old.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String validateSave(MedalConfigEntity request, boolean creating) {
		if (request == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating) {
			String codeErr = validateMedalCode(request.getMedalCode(), true);
			if (codeErr != null) {
				return codeErr;
			}
			if (request.getI18nList() == null || request.getI18nList().isEmpty()) {
				return I18nUtil.getMessage("medal_i18n_required");
			}
			String i18nErr = validateI18nList(request.getI18nList());
			if (i18nErr != null) {
				return i18nErr;
			}
		}
		if (StringUtils.isEmpty(request.getActionType())) {
			return I18nUtil.getMessage("medal_action_type_required");
		}
		if (WelfareActionTypeEnums.fromName(request.getActionType()) == null) {
			return I18nUtil.getMessage("medal_action_type_invalid");
		}
		if (request.getTargetCount() != null && request.getTargetCount() < 1) {
			return I18nUtil.getMessage("base_error");
		}
		if (request.getRewardCoin() != null && request.getRewardCoin() < 0) {
			return I18nUtil.getMessage("base_error");
		}
		return null;
	}

	private String validateMedalCode(String medalCode, boolean creating) {
		if (StringUtils.isEmpty(medalCode) || StringUtils.isEmpty(medalCode.trim())) {
			return I18nUtil.getMessage("medal_code_required");
		}
		String code = normalizeMedalCode(medalCode);
		if (!MEDAL_CODE_PATTERN.matcher(code).matches()) {
			return I18nUtil.getMessage("medal_code_invalid");
		}
		if (creating && medalConfigDao.findByMedalCode(code) != null) {
			return I18nUtil.getMessage("medal_code_exist");
		}
		return null;
	}

	/**
	 * 验证 localized i18n
	 *
	 * @param i18nList
	 * @return
	 */
	private String validateI18nList(List<MedalConfigI18nEntity> i18nList) {
		Set<String> langues = new HashSet<>();
		for (MedalConfigI18nEntity i18n : i18nList) {
			if (i18n == null || StringUtils.isEmpty(i18n.getLangue())
					|| StringUtils.isEmpty(i18n.getMedalName())
					|| StringUtils.isEmpty(i18n.getMedalName().trim())) {
				return I18nUtil.getMessage("medal_i18n_required");
			}
			if (!langues.add(i18n.getLangue().trim())) {
				return I18nUtil.getMessage("base_error");
			}
		}
		return null;
	}

	/**
	 * 保存 localized i18n
	 *
	 * @param entity
	 * @throws Exception
	 */
	private void saveI18nList(MedalConfigEntity entity) throws Exception {
		List<MedalConfigI18nEntity> i18nList = entity.getI18nList();
		if (i18nList == null) {
			return;
		}
		for (MedalConfigI18nEntity src : i18nList) {
			MedalConfigI18nEntity row = new MedalConfigI18nEntity();
			row.setMedalId(entity.getId());
			row.setLangue(src.getLangue().trim());
			row.setMedalName(src.getMedalName().trim());
			row.setSlogan(trimToNull(src.getSlogan()));
			row.setConditionText(trimToNull(src.getConditionText()));
			row.setShareTitle(trimToNull(src.getShareTitle()));
			row.setShareDesc(trimToNull(src.getShareDesc()));
			GenericityUtil.setDate(row);
			medalConfigI18nDao.insert(row);
		}
	}

	/**
	 * 签名图标
	 *
	 * @param row
	 */
	private void signIconUrls(MedalConfigEntity row) {
		if (row == null) {
			return;
		}
		row.setIconKey(mediaUrlService.sign(row.getIconKey()));
		row.setIconLockedKey(mediaUrlService.sign(row.getIconLockedKey()));
		row.setShareBgKey(mediaUrlService.sign(row.getShareBgKey()));
	}

	private static String normalizeMedalCode(String medalCode) {
		return medalCode == null ? null : medalCode.trim().toUpperCase();
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
