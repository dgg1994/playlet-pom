package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.welfare.WithdrawConfigDao;
import com.playlet.internal.entity.welfare.WithdrawConfigEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WithdrawConfigManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 提现配置管理：列表 / 新增 / 修改 / 启停。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WithdrawConfigManageServiceImpl implements WithdrawConfigManageService {

	@Autowired
	private WithdrawConfigDao withdrawConfigDao;

	@Override
	@SysLogAnnotation(module = "提现配置管理", type = "POST", remark = "配置列表")
	public ResponseBase findList(@RequestBody(required = false) WithdrawConfigEntity entity) {
		if (entity == null) {
			entity = new WithdrawConfigEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WithdrawConfigEntity> list = withdrawConfigDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "提现配置管理", type = "POST", remark = "新增配置")
	public ResponseBase save(@RequestBody WithdrawConfigEntity entity) {
		String err = validate(entity, true);
		if (err != null) {
			return setResultError(err);
		}
		normalize(entity);
		if (entity.getStatus() == null) {
			entity.setStatus(1);
		}
		if (entity.getSortWeight() == null) {
			entity.setSortWeight(0);
		}
		if (entity.getServiceFee() == null) {
			entity.setServiceFee(BigDecimal.ZERO);
		}
		if (entity.getMaxWithdrawPointsDay() == null) {
			entity.setMaxWithdrawPointsDay(0);
		}
		WithdrawConfigEntity dup = withdrawConfigDao.findByAssetAndNetwork(
				entity.getAssetCode(), entity.getNetwork(), null);
		if (dup != null) {
			return setResultError(I18nUtil.getMessage("base_info_exist"));
		}
		try {
			GenericityUtil.setDate(entity);
			withdrawConfigDao.insert(entity);
			log.info("withdraw config save id={} assetCode={} network={}",
					entity.getId(), entity.getAssetCode(), entity.getNetwork());
			return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("withdraw config save failed assetCode={} network={}",
					entity.getAssetCode(), entity.getNetwork(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "提现配置管理", type = "POST", remark = "修改配置")
	public ResponseBase update(@RequestBody WithdrawConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WithdrawConfigEntity old = withdrawConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		String err = validate(entity, false);
		if (err != null) {
			return setResultError(err);
		}
		normalize(entity);
		// 未传资产/网络时沿用原值，便于只改限额等字段
		if (StringUtils.isEmpty(entity.getAssetCode())) {
			entity.setAssetCode(old.getAssetCode());
		}
		if (StringUtils.isEmpty(entity.getNetwork())) {
			entity.setNetwork(old.getNetwork());
		}
		WithdrawConfigEntity dup = withdrawConfigDao.findByAssetAndNetwork(
				entity.getAssetCode(), entity.getNetwork(), entity.getId());
		if (dup != null) {
			return setResultError(I18nUtil.getMessage("base_info_exist"));
		}
		try {
			GenericityUtil.updateDate(entity);
			withdrawConfigDao.updateById(entity);
			log.info("withdraw config update id={} assetCode={} network={}",
					entity.getId(), entity.getAssetCode(), entity.getNetwork());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("withdraw config update failed id={}", entity.getId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "提现配置管理", type = "POST", remark = "启停配置")
	public ResponseBase changeStatus(@RequestBody WithdrawConfigEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (entity.getStatus() != 0 && entity.getStatus() != 1) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WithdrawConfigEntity old = withdrawConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WithdrawConfigEntity upd = new WithdrawConfigEntity();
		upd.setId(old.getId());
		upd.setStatus(entity.getStatus());
		try {
			GenericityUtil.updateDate(upd);
			withdrawConfigDao.updateById(upd);
			log.info("withdraw config changeStatus id={} status={}", old.getId(), entity.getStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("withdraw config changeStatus failed id={}", old.getId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	/** 新增必填校验；修改时仅校验传入字段。 */
	private String validate(WithdrawConfigEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating) {
			if (StringUtils.isEmpty(entity.getAssetCode()) || StringUtils.isEmpty(entity.getNetwork())) {
				return I18nUtil.getMessage("base_error");
			}
			if (entity.getPointsPerUnit() == null || entity.getPointsPerUnit() <= 0) {
				return I18nUtil.getMessage("base_error");
			}
			if (entity.getMinWithdrawPoints() == null || entity.getMinWithdrawPoints() < 0) {
				return I18nUtil.getMessage("base_error");
			}
		} else {
			if (entity.getPointsPerUnit() != null && entity.getPointsPerUnit() <= 0) {
				return I18nUtil.getMessage("base_error");
			}
			if (entity.getMinWithdrawPoints() != null && entity.getMinWithdrawPoints() < 0) {
				return I18nUtil.getMessage("base_error");
			}
		}
		if (entity.getServiceFee() != null && entity.getServiceFee().compareTo(BigDecimal.ZERO) < 0) {
			return I18nUtil.getMessage("base_error");
		}
		if (entity.getMaxWithdrawPointsDay() != null && entity.getMaxWithdrawPointsDay() < 0) {
			return I18nUtil.getMessage("base_error");
		}
		if (entity.getStatus() != null && entity.getStatus() != 0 && entity.getStatus() != 1) {
			return I18nUtil.getMessage("base_error");
		}
		return null;
	}

	private void normalize(WithdrawConfigEntity entity) {
		if (entity.getAssetCode() != null) {
			entity.setAssetCode(entity.getAssetCode().trim().toUpperCase());
		}
		if (entity.getNetwork() != null) {
			entity.setNetwork(entity.getNetwork().trim().toUpperCase());
		}
		if (entity.getRemark() != null) {
			entity.setRemark(entity.getRemark().trim());
		}
	}
}
