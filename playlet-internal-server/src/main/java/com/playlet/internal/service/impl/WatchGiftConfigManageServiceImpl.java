package com.playlet.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.welfare.WatchGiftGlobalConfigDao;
import com.playlet.internal.entity.welfare.WatchGiftGlobalConfigEntity;
import com.playlet.internal.service.WatchGiftConfigManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WatchGiftConfigManageServiceImpl implements WatchGiftConfigManageService {

	@Autowired
	private WatchGiftGlobalConfigDao watchGiftGlobalConfigDao;

	@Override
	@SysLogAnnotation(module = "观影礼全局配置", type = "POST", remark = "配置列表")
	public ResponseBase findList(@RequestBody(required = false) WatchGiftGlobalConfigEntity entity) {
		QueryWrapper<WatchGiftGlobalConfigEntity> qw = new QueryWrapper<>();
		if (entity != null && entity.getStatus() != null) {
			qw.eq("status", entity.getStatus());
		}
		qw.orderByDesc("id");
		List<WatchGiftGlobalConfigEntity> list = watchGiftGlobalConfigDao.selectList(qw);
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "观影礼全局配置", type = "POST", remark = "编辑配置")
	public ResponseBase update(@RequestBody WatchGiftGlobalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			WatchGiftGlobalConfigEntity old = watchGiftGlobalConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validate(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			if (entity.getTimezone() != null) {
				entity.setTimezone(entity.getTimezone().trim());
			}
			GenericityUtil.updateDate(entity);
			watchGiftGlobalConfigDao.updateById(entity);
			if (entity.getStatus() != null && entity.getStatus() == 1) {
				watchGiftGlobalConfigDao.disableOthers(entity.getId());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "观影礼全局配置", type = "POST", remark = "启停配置")
	public ResponseBase changeStatus(@RequestBody WatchGiftGlobalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (entity.getStatus() != 0 && entity.getStatus() != 1) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			WatchGiftGlobalConfigEntity old = watchGiftGlobalConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			WatchGiftGlobalConfigEntity upd = new WatchGiftGlobalConfigEntity();
			upd.setId(old.getId());
			upd.setStatus(entity.getStatus());
			GenericityUtil.updateDate(upd);
			watchGiftGlobalConfigDao.updateById(upd);
			if (entity.getStatus() == 1) {
				watchGiftGlobalConfigDao.disableOthers(old.getId());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String validate(WatchGiftGlobalConfigEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && StringUtils.isEmpty(entity.getTimezone())) {
			return "timezone 不能为空";
		}
		if (entity.getMinReportIntervalSec() != null && entity.getMinReportIntervalSec() < 0) {
			return "minReportIntervalSec 须 >= 0";
		}
		if (entity.getMaxDeltaSecPerReport() != null && entity.getMaxDeltaSecPerReport() < 1) {
			return "maxDeltaSecPerReport 须 >= 1";
		}
		if (entity.getMaxDailySeconds() != null && entity.getMaxDailySeconds() < 1) {
			return "maxDailySeconds 须 >= 1";
		}
		if (entity.getStatus() != null && entity.getStatus() != 0 && entity.getStatus() != 1) {
			return "status 仅支持 0/1";
		}
		return null;
	}

}
