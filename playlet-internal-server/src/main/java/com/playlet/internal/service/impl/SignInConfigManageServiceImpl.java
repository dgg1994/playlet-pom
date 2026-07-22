package com.playlet.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.welfare.SignInGlobalConfigDao;
import com.playlet.internal.entity.welfare.SignInGlobalConfigEntity;
import com.playlet.internal.enums.SignInCycleModeEnums;
import com.playlet.internal.service.SignInConfigManageService;
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
public class SignInConfigManageServiceImpl implements SignInConfigManageService {

	@Autowired
	private SignInGlobalConfigDao signInGlobalConfigDao;

	@Override
	@SysLogAnnotation(module = "签到全局配置", type = "POST", remark = "配置列表")
	public ResponseBase findList(@RequestBody(required = false) SignInGlobalConfigEntity entity) {
		QueryWrapper<SignInGlobalConfigEntity> qw = new QueryWrapper<>();
		if (entity != null && entity.getStatus() != null) {
			qw.eq("status", entity.getStatus());
		}
		qw.orderByDesc("id");
		List<SignInGlobalConfigEntity> list = signInGlobalConfigDao.selectList(qw);
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "签到全局配置", type = "POST", remark = "编辑配置")
	public ResponseBase update(@RequestBody SignInGlobalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			SignInGlobalConfigEntity old = signInGlobalConfigDao.selectById(entity.getId());
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
			if (entity.getCycleMode() != null && !entity.getCycleMode().trim().isEmpty()) {
				SignInCycleModeEnums cycleMode = SignInCycleModeEnums.fromCode(entity.getCycleMode());
				if (cycleMode != null) {
					entity.setCycleMode(cycleMode.getCode());
				}
			}
			GenericityUtil.updateDate(entity);
			signInGlobalConfigDao.updateById(entity);
			if (entity.getStatus() != null && entity.getStatus() == 1) {
				signInGlobalConfigDao.disableOthers(entity.getId());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	@SysLogAnnotation(module = "签到全局配置", type = "POST", remark = "启停配置")
	public ResponseBase changeStatus(@RequestBody SignInGlobalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			if (entity.getStatus() != 0 && entity.getStatus() != 1) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			SignInGlobalConfigEntity old = signInGlobalConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			SignInGlobalConfigEntity upd = new SignInGlobalConfigEntity();
			upd.setId(old.getId());
			upd.setStatus(entity.getStatus());
			GenericityUtil.updateDate(upd);
			signInGlobalConfigDao.updateById(upd);
			if (entity.getStatus() == 1) {
				signInGlobalConfigDao.disableOthers(old.getId());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String validate(SignInGlobalConfigEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && StringUtils.isEmpty(entity.getTimezone())) {
			return "timezone 不能为空";
		}
		if (entity.getCycleMode() != null && !SignInCycleModeEnums.isValid(entity.getCycleMode())) {
			return "cycleMode 仅支持 CAP / LOOP";
		}
		if (entity.getCycleDays() != null && entity.getCycleDays() < 1) {
			return "cycleDays 须 >= 1";
		}
		if (entity.getMakeupEnabled() != null
				&& entity.getMakeupEnabled() != 0 && entity.getMakeupEnabled() != 1) {
			return "makeupEnabled 仅支持 0/1";
		}
		if (entity.getStatus() != null && entity.getStatus() != 0 && entity.getStatus() != 1) {
			return "status 仅支持 0/1";
		}
		if (entity.getMakeupWindowDays() != null && entity.getMakeupWindowDays() < 0) {
			return "makeupWindowDays 须 >= 0";
		}
		if (entity.getMakeupMonthLimit() != null && entity.getMakeupMonthLimit() < 0) {
			return "makeupMonthLimit 须 >= 0";
		}
		if (entity.getMakeupCostCard() != null && entity.getMakeupCostCard() < 0) {
			return "makeupCostCard 须 >= 0";
		}
		if (entity.getMakeupBuyMonthLimit() != null && entity.getMakeupBuyMonthLimit() < 0) {
			return "makeupBuyMonthLimit 须 >= 0";
		}
		if (entity.getMakeupBuyPriceCoin() != null && entity.getMakeupBuyPriceCoin() < 0) {
			return "makeupBuyPriceCoin 须 >= 0";
		}
		if (entity.getMakeupCostCoin() != null && entity.getMakeupCostCoin() < 0) {
			return "makeupCostCoin 须 >= 0";
		}
		return null;
	}

}
