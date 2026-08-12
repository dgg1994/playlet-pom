package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.security.SensitiveWordDao;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import com.playlet.internal.service.SensitiveWordManageService;
import com.playlet.internal.service.SensitiveWordService;
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

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SensitiveWordManageServiceImpl implements SensitiveWordManageService {

	@Autowired
	private SensitiveWordDao sensitiveWordDao;
	@Autowired
	private SensitiveWordService sensitiveWordService;

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "敏感词列表")
	public ResponseBase findList(@RequestBody SensitiveWordEntity entity) {
		if (entity == null) {
			entity = new SensitiveWordEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<SensitiveWordEntity> list = sensitiveWordDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "敏感词详情")
	public ResponseBase detail(@RequestBody SensitiveWordEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SensitiveWordEntity row = sensitiveWordDao.selectById(entity.getId());
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "新增敏感词")
	public ResponseBase save(@RequestBody SensitiveWordEntity entity) {
		try {
			String err = validateSave(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			entity.setWord(normalizeWord(entity.getWord()));
			if (entity.getStatus() == null) {
				entity.setStatus(1);
			}
			if (entity.getLevel() == null) {
				entity.setLevel(1);
			}
			GenericityUtil.setDate(entity);
			try {
				sensitiveWordDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError(I18nUtil.getMessage("sensitive_word_exist"));
			}
			sensitiveWordService.reload();
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "编辑敏感词")
	public ResponseBase update(@RequestBody SensitiveWordEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			SensitiveWordEntity old = sensitiveWordDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateSave(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			if (entity.getWord() != null) {
				String word = normalizeWord(entity.getWord());
				SensitiveWordEntity dup = sensitiveWordDao.findByWord(word);
				if (dup != null && !dup.getId().equals(old.getId())) {
					return setResultError(I18nUtil.getMessage("sensitive_word_exist"));
				}
				entity.setWord(word);
			}
			GenericityUtil.updateDate(entity);
			sensitiveWordDao.updateById(entity);
			sensitiveWordService.reload();
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "启停敏感词")
	public ResponseBase changeStatus(@RequestBody SensitiveWordEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (entity.getStatus() != 0 && entity.getStatus() != 1) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			SensitiveWordEntity old = sensitiveWordDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			SensitiveWordEntity upd = new SensitiveWordEntity();
			upd.setId(old.getId());
			upd.setStatus(entity.getStatus());
			GenericityUtil.updateDate(upd);
			sensitiveWordDao.updateById(upd);
			sensitiveWordService.reload();
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "刷新词库")
	public ResponseBase reload() {
		sensitiveWordService.reload();
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/**
	 * 验证保存
	 *
	 * @param request
	 * @param creating
	 * @return
	 */
	private String validateSave(SensitiveWordEntity request, boolean creating) {
		if (request == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && StringUtils.isEmpty(request.getWord())) {
			return I18nUtil.getMessage("sensitive_word_required");
		}
		if (request.getWord() != null && StringUtils.isEmpty(request.getWord().trim())) {
			return I18nUtil.getMessage("sensitive_word_required");
		}
		if (request.getLevel() != null && (request.getLevel() < 1 || request.getLevel() > 3)) {
			return I18nUtil.getMessage("sensitive_word_level_invalid");
		}
		if (creating) {
			SensitiveWordEntity exist = sensitiveWordDao.findByWord(normalizeWord(request.getWord()));
			if (exist != null) {
				return I18nUtil.getMessage("sensitive_word_exist");
			}
		}
		return null;
	}

	/**
	 * 正则化敏感词
	 *
	 * @param word
	 * @return
	 */
	private static String normalizeWord(String word) {
		return word == null ? null : word.trim();
	}
}
