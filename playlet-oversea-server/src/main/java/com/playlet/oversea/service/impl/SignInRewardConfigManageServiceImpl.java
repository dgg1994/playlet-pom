package com.playlet.oversea.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.welfare.SignInRewardConfigDao;
import com.playlet.oversea.entity.welfare.SignInRewardConfigEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.SignInRewardConfigManageService;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端签到奖励阶梯配置。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class SignInRewardConfigManageServiceImpl implements SignInRewardConfigManageService {

	/** status：启用 */
	private static final int STATUS_ENABLED = 1;
	/** status：停用 */
	private static final int STATUS_DISABLED = 0;

	@Autowired
	private SignInRewardConfigDao signInRewardConfigDao;

	@Override
	@SysLogAnnotation(module = "签到奖励阶梯", type = "POST", remark = "列表")
	public ResponseBase findList(@RequestBody(required = false) SignInRewardConfigEntity entity) {
		if (entity == null) {
			entity = new SignInRewardConfigEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		QueryWrapper<SignInRewardConfigEntity> qw = new QueryWrapper<>();
		if (entity.getStatus() != null) {
			qw.eq("status", entity.getStatus());
		}
		qw.orderByAsc("day_index").orderByDesc("id");
		List<SignInRewardConfigEntity> list = signInRewardConfigDao.selectList(qw);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "签到奖励阶梯", type = "POST", remark = "新增")
	public ResponseBase save(@RequestBody SignInRewardConfigEntity entity) {
		String err = validate(entity, true);
		if (err != null) {
			return setResultError(err);
		}
		if (signInRewardConfigDao.countByDayIndex(entity.getDayIndex(), null) > 0) {
			return setResultError("dayIndex 已存在");
		}
		if (entity.getStatus() == null) {
			entity.setStatus(STATUS_ENABLED);
		}
		if (entity.getRemark() != null) {
			entity.setRemark(entity.getRemark().trim());
		}
		try {
			GenericityUtil.setDate(entity);
			signInRewardConfigDao.insert(entity);
		} catch (Exception e) {
			log.error("signIn reward config save failed dayIndex={}", entity.getDayIndex(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("signIn reward config saved id={} dayIndex={} rewardCoin={}",
				entity.getId(), entity.getDayIndex(), entity.getRewardCoin());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "签到奖励阶梯", type = "POST", remark = "编辑")
	public ResponseBase update(@RequestBody SignInRewardConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SignInRewardConfigEntity old = signInRewardConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		String err = validate(entity, false);
		if (err != null) {
			return setResultError(err);
		}
		Integer dayIndex = entity.getDayIndex() != null ? entity.getDayIndex() : old.getDayIndex();
		if (signInRewardConfigDao.countByDayIndex(dayIndex, entity.getId()) > 0) {
			return setResultError("dayIndex 已存在");
		}
		if (entity.getRemark() != null) {
			entity.setRemark(entity.getRemark().trim());
		}
		try {
			GenericityUtil.updateDate(entity);
			signInRewardConfigDao.updateById(entity);
		} catch (Exception e) {
			log.error("signIn reward config update failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("signIn reward config updated id={} dayIndex={} rewardCoin={}",
				entity.getId(), dayIndex, entity.getRewardCoin());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "签到奖励阶梯", type = "POST", remark = "启停")
	public ResponseBase changeStatus(@RequestBody SignInRewardConfigEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (entity.getStatus() != STATUS_DISABLED && entity.getStatus() != STATUS_ENABLED) {
			return setResultError("status 仅支持 0/1");
		}
		SignInRewardConfigEntity old = signInRewardConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		SignInRewardConfigEntity upd = new SignInRewardConfigEntity();
		upd.setId(old.getId());
		upd.setStatus(entity.getStatus());
		try {
			GenericityUtil.updateDate(upd);
			signInRewardConfigDao.updateById(upd);
		} catch (Exception e) {
			log.error("signIn reward config changeStatus failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("signIn reward config status changed id={} status={}", entity.getId(), entity.getStatus());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "签到奖励阶梯", type = "POST", remark = "删除")
	public ResponseBase delete(@RequestBody SignInRewardConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		SignInRewardConfigEntity old = signInRewardConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		try {
			signInRewardConfigDao.deleteById(entity.getId());
		} catch (Exception e) {
			log.error("signIn reward config delete failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("signIn reward config deleted id={} dayIndex={}", old.getId(), old.getDayIndex());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 校验 dayIndex / rewardCoin / status */
	private String validate(SignInRewardConfigEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating || entity.getDayIndex() != null) {
			if (entity.getDayIndex() == null || entity.getDayIndex() < 1) {
				return "dayIndex 须 >= 1";
			}
		}
		if (creating || entity.getRewardCoin() != null) {
			if (entity.getRewardCoin() == null || entity.getRewardCoin() < 0) {
				return "rewardCoin 须 >= 0";
			}
		}
		if (entity.getStatus() != null
				&& entity.getStatus() != STATUS_DISABLED
				&& entity.getStatus() != STATUS_ENABLED) {
			return "status 仅支持 0/1";
		}
		return null;
	}
}
