package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.SensitiveWordConstants;
import com.playlet.internal.dao.security.SensitiveWordDao;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.security.SensitiveWordEntity;
import com.playlet.internal.entity.system.SysUserEntity;
import com.playlet.internal.enums.SensitiveWordCategoryEnums;
import com.playlet.internal.enums.SensitiveWordStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.SensitiveWordManageService;
import com.playlet.internal.service.SensitiveWordService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端敏感词：新增待审、通过/驳回/禁用、仅已生效进 DFA。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SensitiveWordManageServiceImpl implements SensitiveWordManageService {

	@Autowired
	private SensitiveWordDao sensitiveWordDao;
	@Autowired
	private SensitiveWordService sensitiveWordService;
	@Autowired
	private SysUserDao sysUserDao;

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "敏感词列表")
	public ResponseBase findList(@RequestBody(required = false) SensitiveWordEntity entity) {
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
	public ResponseBase save(HttpServletRequest request, @RequestBody SensitiveWordEntity entity) {
		try {
			String err = validateSave(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			entity.setWord(normalizeWord(entity.getWord()));
			entity.setCategory(SensitiveWordCategoryEnums.ofCode(entity.getCategory()).getCode());
			entity.setStatus(SensitiveWordStatusEnums.PENDING.getIndex());
			fillCreator(request, entity);
			GenericityUtil.setDate(entity);
			try {
				sensitiveWordDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError(I18nUtil.getMessage("sensitive_word_exist"));
			}
			// 插入后按 id 生成业务编号
			entity.setWordCode(buildWordCode(entity.getId()));
			sensitiveWordDao.updateById(entity);
			log.info("sensitive word save id={} code={} word={}", entity.getId(), entity.getWordCode(), entity.getWord());
			// 待审核不进词库，无需 reload
			return setResultSuccess(entity.getId(), I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("sensitive word save biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("sensitive word save failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "编辑敏感词")
	public ResponseBase update(HttpServletRequest request, @RequestBody SensitiveWordEntity entity) {
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
			boolean wordChanged = false;
			if (entity.getWord() != null) {
				String word = normalizeWord(entity.getWord());
				SensitiveWordEntity dup = sensitiveWordDao.findByWord(word);
				if (dup != null && !dup.getId().equals(old.getId())) {
					return setResultError(I18nUtil.getMessage("sensitive_word_exist"));
				}
				wordChanged = !word.equals(old.getWord());
				entity.setWord(word);
			}
			if (entity.getCategory() != null) {
				SensitiveWordCategoryEnums cat = SensitiveWordCategoryEnums.ofCode(entity.getCategory());
				if (cat == null) {
					return setResultError("category 无效");
				}
				entity.setCategory(cat.getCode());
			}
			// 已生效词修改文案后重新待审，避免未审词进生产词库
			boolean needReload = SensitiveWordStatusEnums.ACTIVE.getIndex().equals(old.getStatus());
			if (needReload && wordChanged) {
				entity.setStatus(SensitiveWordStatusEnums.PENDING.getIndex());
			}
			GenericityUtil.updateDate(entity);
			sensitiveWordDao.updateById(entity);
			if (needReload) {
				sensitiveWordService.reload();
			}
			log.info("sensitive word update id={} wordChanged={}", entity.getId(), wordChanged);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("sensitive word update biz error id={}", entity == null ? null : entity.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("sensitive word update failed id={}", entity == null ? null : entity.getId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "审核通过")
	public ResponseBase approve(HttpServletRequest request, @RequestBody SensitiveWordEntity entity) {
		return changeStatus(request, entity, SensitiveWordStatusEnums.PENDING, SensitiveWordStatusEnums.ACTIVE, true);
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "审核驳回")
	public ResponseBase reject(HttpServletRequest request, @RequestBody SensitiveWordEntity entity) {
		return changeStatus(request, entity, SensitiveWordStatusEnums.PENDING, SensitiveWordStatusEnums.REJECTED, false);
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "禁用敏感词")
	public ResponseBase disable(HttpServletRequest request, @RequestBody SensitiveWordEntity entity) {
		return changeStatus(request, entity, SensitiveWordStatusEnums.ACTIVE, SensitiveWordStatusEnums.DISABLED, true);
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "GET", remark = "删除敏感词")
	public ResponseBase delete(Integer id) {
		try {
			if (id == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			SensitiveWordEntity entity = sensitiveWordDao.selectById(id);
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			boolean wasActive = SensitiveWordStatusEnums.ACTIVE.getIndex().equals(entity.getStatus());
			sensitiveWordDao.deleteById(id);
			if (wasActive) {
				sensitiveWordService.reload();
			}
			log.info("sensitive word delete id={}", id);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("sensitive word delete biz error id={}", id, e);
			throw e;
		} catch (Exception e) {
			log.error("sensitive word delete failed id={}", id, e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "敏感词管理", type = "POST", remark = "刷新词库")
	public ResponseBase reload() {
		sensitiveWordService.reload();
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private ResponseBase changeStatus(HttpServletRequest request, SensitiveWordEntity entity,
			SensitiveWordStatusEnums expectFrom, SensitiveWordStatusEnums to, boolean reload) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			SensitiveWordEntity old = sensitiveWordDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			SensitiveWordEntity upd = new SensitiveWordEntity();
			upd.setId(old.getId());
			upd.setStatus(to.getIndex());
			upd.setAuditorId(adminId);
			upd.setAuditTime(new Date());
			if (!StringUtils.isEmpty(entity.getAuditRemark())) {
				upd.setAuditRemark(entity.getAuditRemark().trim());
			}
			GenericityUtil.updateDate(upd);
			sensitiveWordDao.updateById(upd);
			if (reload) {
				sensitiveWordService.reload();
			}
			log.info("sensitive word status id={} {} -> {}", old.getId(), expectFrom, to);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("sensitive word status biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("sensitive word status failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	private void fillCreator(HttpServletRequest request, SensitiveWordEntity entity) {
		Integer adminId = SysUserTokenUtil.resolveAdminId(request);
		entity.setCreatorId(adminId);
		if (adminId == null) {
			entity.setCreatorName(SensitiveWordConstants.CREATOR_NAME_SYSTEM);
			return;
		}
		SysUserEntity admin = sysUserDao.findById(adminId);
		if (admin == null) {
			entity.setCreatorName(SensitiveWordConstants.CREATOR_NAME_SYSTEM);
			return;
		}
		String name = !StringUtils.isEmpty(admin.getUsername()) ? admin.getUsername() : admin.getAcctive();
		entity.setCreatorName(StringUtils.isEmpty(name) ? SensitiveWordConstants.CREATOR_NAME_SYSTEM : name);
	}

	private static String buildWordCode(Long id) {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		return SensitiveWordConstants.WORD_CODE_PREFIX + year + String.format("%04d", id == null ? 0 : id);
	}

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
			if (StringUtils.isEmpty(request.getCategory())
					|| !SensitiveWordCategoryEnums.isValid(request.getCategory())) {
				return "category 须为 FORBIDDEN 或 AD";
			}
			SensitiveWordEntity exist = sensitiveWordDao.findByWord(normalizeWord(request.getWord()));
			if (exist != null) {
				return I18nUtil.getMessage("sensitive_word_exist");
			}
		} else if (request.getCategory() != null && !SensitiveWordCategoryEnums.isValid(request.getCategory())) {
			return "category 须为 FORBIDDEN 或 AD";
		}
		return null;
	}

	private static String normalizeWord(String word) {
		return word == null ? null : word.trim();
	}
}
