package com.playlet.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.welfare.WatchGiftRewardConfigDao;
import com.playlet.internal.entity.welfare.WatchGiftRewardConfigEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WatchGiftRewardConfigManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端观影礼奖励阶梯配置。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WatchGiftRewardConfigManageServiceImpl implements WatchGiftRewardConfigManageService {

	/** status：启用 */
	private static final int STATUS_ENABLED = 1;
	/** status：停用 */
	private static final int STATUS_DISABLED = 0;

	@Autowired
	private WatchGiftRewardConfigDao watchGiftRewardConfigDao;

	@Override
	@SysLogAnnotation(module = "观影礼奖励阶梯", type = "POST", remark = "列表")
	public ResponseBase findList(@RequestBody(required = false) WatchGiftRewardConfigEntity entity) {
		if (entity == null) {
			entity = new WatchGiftRewardConfigEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		QueryWrapper<WatchGiftRewardConfigEntity> qw = new QueryWrapper<>();
		if (entity.getStatus() != null) {
			qw.eq("status", entity.getStatus());
		}
		if (entity.getGearIndex() != null) {
			qw.eq("gear_index", entity.getGearIndex());
		}
		qw.orderByAsc("target_seconds").orderByAsc("gear_index").orderByDesc("id");
		List<WatchGiftRewardConfigEntity> list = watchGiftRewardConfigDao.selectList(qw);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "观影礼奖励阶梯", type = "POST", remark = "新增")
	public ResponseBase save(@RequestBody WatchGiftRewardConfigEntity entity) {
		String err = validate(entity, true);
		if (err != null) {
			return setResultError(err);
		}
		if (watchGiftRewardConfigDao.countByGearIndex(entity.getGearIndex(), null) > 0) {
			return setResultError("gearIndex 已存在");
		}
		if (entity.getStatus() == null) {
			entity.setStatus(STATUS_ENABLED);
		}
		if (entity.getRemark() != null) {
			entity.setRemark(entity.getRemark().trim());
		}
		try {
			GenericityUtil.setDate(entity);
			watchGiftRewardConfigDao.insert(entity);
		} catch (BaseException e) {
			log.error("watchGift reward config save biz failed gearIndex={}", entity.getGearIndex(), e);
			throw e;
		} catch (Exception e) {
			log.error("watchGift reward config save failed gearIndex={}", entity.getGearIndex(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("watchGift reward config saved id={} gearIndex={} targetSeconds={} rewardCoin={}",
				entity.getId(), entity.getGearIndex(), entity.getTargetSeconds(), entity.getRewardCoin());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "观影礼奖励阶梯", type = "POST", remark = "编辑")
	public ResponseBase update(@RequestBody WatchGiftRewardConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WatchGiftRewardConfigEntity old = watchGiftRewardConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		String err = validate(entity, false);
		if (err != null) {
			return setResultError(err);
		}
		Integer gearIndex = entity.getGearIndex() != null ? entity.getGearIndex() : old.getGearIndex();
		if (watchGiftRewardConfigDao.countByGearIndex(gearIndex, entity.getId()) > 0) {
			return setResultError("gearIndex 已存在");
		}
		if (entity.getRemark() != null) {
			entity.setRemark(entity.getRemark().trim());
		}
		try {
			GenericityUtil.updateDate(entity);
			watchGiftRewardConfigDao.updateById(entity);
		} catch (BaseException e) {
			log.error("watchGift reward config update biz failed id={}", entity.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("watchGift reward config update failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("watchGift reward config updated id={} gearIndex={} targetSeconds={} rewardCoin={}",
				entity.getId(), gearIndex, entity.getTargetSeconds(), entity.getRewardCoin());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "观影礼奖励阶梯", type = "POST", remark = "启停")
	public ResponseBase changeStatus(@RequestBody WatchGiftRewardConfigEntity entity) {
		if (entity == null || entity.getId() == null || entity.getStatus() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (entity.getStatus() != STATUS_DISABLED && entity.getStatus() != STATUS_ENABLED) {
			return setResultError("status 仅支持 0/1");
		}
		WatchGiftRewardConfigEntity old = watchGiftRewardConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		WatchGiftRewardConfigEntity upd = new WatchGiftRewardConfigEntity();
		upd.setId(old.getId());
		upd.setStatus(entity.getStatus());
		try {
			GenericityUtil.updateDate(upd);
			watchGiftRewardConfigDao.updateById(upd);
		} catch (BaseException e) {
			log.error("watchGift reward config changeStatus biz failed id={}", entity.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("watchGift reward config changeStatus failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("watchGift reward config status changed id={} status={}", entity.getId(), entity.getStatus());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "观影礼奖励阶梯", type = "POST", remark = "删除")
	public ResponseBase delete(@RequestBody WatchGiftRewardConfigEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WatchGiftRewardConfigEntity old = watchGiftRewardConfigDao.selectById(entity.getId());
		if (old == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		try {
			watchGiftRewardConfigDao.deleteById(entity.getId());
		} catch (BaseException e) {
			log.error("watchGift reward config delete biz failed id={}", entity.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("watchGift reward config delete failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("watchGift reward config deleted id={} gearIndex={}", old.getId(), old.getGearIndex());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 校验 gearIndex / targetSeconds / rewardCoin / status */
	private String validate(WatchGiftRewardConfigEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating || entity.getGearIndex() != null) {
			if (entity.getGearIndex() == null || entity.getGearIndex() < 1) {
				return "gearIndex 须 >= 1";
			}
		}
		if (creating || entity.getTargetSeconds() != null) {
			if (entity.getTargetSeconds() == null || entity.getTargetSeconds() < 1) {
				return "targetSeconds 须 >= 1";
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
