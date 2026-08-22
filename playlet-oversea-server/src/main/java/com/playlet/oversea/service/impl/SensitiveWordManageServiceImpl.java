package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.security.SensitiveWordDao;
import com.playlet.oversea.entity.security.SensitiveWordEntity;
import com.playlet.oversea.service.SensitiveWordManageService;
import com.playlet.oversea.service.SensitiveWordService;
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
	public ResponseBase delete(Integer id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SensitiveWordEntity entity = sensitiveWordDao.selectById(id);
		if (entity == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		sensitiveWordDao.deleteById(id);
		sensitiveWordService.reload();
		return setResultSuccess(I18nUtil.getMessage("base_success"));
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
