package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.welfare.WelfareTaskDao;
import com.playlet.internal.dao.welfare.WelfareTaskI18nDao;
import com.playlet.internal.entity.welfare.WelfareTaskEntity;
import com.playlet.internal.entity.welfare.WelfareTaskI18nEntity;
import com.playlet.internal.service.WelfareTaskManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WelfareTaskManageServiceImpl implements WelfareTaskManageService {

	@Autowired
	private WelfareTaskDao welfareTaskDao;
	@Autowired
	private WelfareTaskI18nDao welfareTaskI18nDao;

	@Override
	@SysLogAnnotation(module = "福利任务", type = "POST", remark = "任务列表")
	public ResponseBase findList(@RequestBody WelfareTaskEntity entity) {
		if (entity == null) {
			entity = new WelfareTaskEntity();
		}
		List<WelfareTaskEntity> list = welfareTaskDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		// 获取语言
		String language = LanguageContext.getLanguage();
		for (WelfareTaskEntity welfareTaskEntity : list) {
			welfareTaskEntity.setTaskName(welfareTaskI18nDao.selectNameById(welfareTaskEntity.getId(),language));
		}
		List<WelfareTaskEntity> welfareTaskEntities = GenericityUtil.Page(list, entity.getPageNumber(), entity.getPageSize());
		PageInfo<WelfareTaskEntity> page = new PageInfo<>(welfareTaskEntities);
		page.setTotal(list.size());
		return setResultSuccess(page,I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "福利任务", type = "POST", remark = "任务详情")
	public ResponseBase detail(@RequestBody WelfareTaskEntity entity) {
		if (entity == null || (entity.getId() == null)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WelfareTaskEntity row = null;
		if (entity.getId() != null) {
			row = welfareTaskDao.selectById(entity.getId());
		}
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		row.setI18nList(welfareTaskI18nDao.findByTaskCode(row.getId()));
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "福利任务", type = "POST", remark = "新增任务")
	public ResponseBase save(@RequestBody WelfareTaskEntity entity) {
		try {
			String err = validateSave(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			if (entity.getStatus() == null) {
				entity.setStatus(WelfareTaskEntity.STATUS_ENABLED);
			}
			GenericityUtil.setDate(entity);
			welfareTaskDao.insert(entity);
			saveI18nList(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "福利任务", type = "POST", remark = "编辑任务")
	public ResponseBase update(@RequestBody WelfareTaskEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			WelfareTaskEntity old = welfareTaskDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateSave(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			GenericityUtil.updateDate(entity);
			welfareTaskDao.updateById(entity);
			if (entity.getI18nList() != null) {
				if (entity.getI18nList().isEmpty()) {
					return setResultError(I18nUtil.getMessage("task_i18n_required"));
				}
				String i18nErr = validateI18nList(entity.getI18nList());
				if (i18nErr != null) {
					return setResultError(i18nErr);
				}
				welfareTaskI18nDao.deleteByTaskCode(old.getId());
				saveI18nList(entity);
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "福利任务", type = "POST", remark = "启停任务")
	public ResponseBase changeStatus(@RequestBody WelfareTaskEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (entity.getStatus() != 0 && entity.getStatus() != 1) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WelfareTaskEntity old = welfareTaskDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WelfareTaskEntity upd = new WelfareTaskEntity();
		upd.setId(old.getId());
		upd.setStatus(entity.getStatus());
		try {
			GenericityUtil.updateDate(upd);
			welfareTaskDao.updateById(upd);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "福利任务", type = "POST", remark = "删除任务")
	public ResponseBase delete(@RequestBody WelfareTaskEntity entity) {
        try {
            if (entity == null || entity.getId() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            WelfareTaskEntity old = welfareTaskDao.selectById(entity.getId());
            if (old == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            welfareTaskI18nDao.deleteByTaskCode(old.getId());
            welfareTaskDao.deleteById(old.getId());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


	/**
	 * 验证参数
	 * @param request 请求
	 * @param creating 是否创建
	 * @return
	 */
	private String validateSave(WelfareTaskEntity request, boolean creating) {
		if (request == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating) {
			if (request.getI18nList() == null || request.getI18nList().isEmpty()) {
				return I18nUtil.getMessage("task_i18n_required");
			}
			String i18nErr = validateI18nList(request.getI18nList());
			if (i18nErr != null) {
				return i18nErr;
			}
		}
		if (request.getCycleType() != null
				&& (request.getCycleType() < 1 || request.getCycleType() > 4)) {
			return I18nUtil.getMessage("base_error");
		}
		if (request.getTargetCount() != null && request.getTargetCount() < 1) {
			return I18nUtil.getMessage("base_error");
		}
		if (request.getRewardCoin() != null && request.getRewardCoin() < 0) {
			return I18nUtil.getMessage("base_error");
		}
		if (request.getAdBoostCoin() != null && request.getAdBoostCoin() < 0) {
			return I18nUtil.getMessage("base_error");
		}
		return null;
	}

	/**
	 * 验证多语言文案
	 * @param i18nList 多语言文案列表
	 * @return
	 */
	private String validateI18nList(List<WelfareTaskI18nEntity> i18nList) {
		Set<String> langues = new HashSet<>();
		for (WelfareTaskI18nEntity i18n : i18nList) {
			if (i18n == null || StringUtils.isEmpty(i18n.getLangue()) || StringUtils.isEmpty(i18n.getTaskName())
					|| StringUtils.isEmpty(i18n.getTaskName().trim())) {
				return I18nUtil.getMessage("task_i18n_required");
			}
			String lang = i18n.getLangue().trim();
			if (!langues.add(lang)) {
				return I18nUtil.getMessage("base_error");
			}
		}
		return null;
	}

	/**
	 * 保存多语言文案
	 * @param entity 实体
	 * @throws Exception
	 */
	private void saveI18nList(WelfareTaskEntity entity) throws Exception {
		List<WelfareTaskI18nEntity> i18nList = entity.getI18nList();
		if (i18nList == null) {
			return;
		}
		for (WelfareTaskI18nEntity src : i18nList) {
			WelfareTaskI18nEntity row = new WelfareTaskI18nEntity();
			row.setTaskId(entity.getId());
			row.setLangue(src.getLangue().trim());
			row.setTaskName(src.getTaskName().trim());
			row.setTaskDesc(src.getTaskDesc() == null ? "" : src.getTaskDesc().trim());
			GenericityUtil.setDate(row);
			welfareTaskI18nDao.insert(row);
		}
	}
}
