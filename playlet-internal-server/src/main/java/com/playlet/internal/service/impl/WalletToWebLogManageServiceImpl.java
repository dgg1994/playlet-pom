package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.wallet.WalletAccountDao;
import com.playlet.internal.dao.wallet.WalletLogDao;
import com.playlet.internal.dao.wallet.WalletToWebLogDao;
import com.playlet.internal.entity.wallet.WalletAccountEntity;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletToWebLogEntity;
import com.playlet.internal.enums.WalletLogStatusEnums;
import com.playlet.internal.enums.WalletRecordStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.WalletToWebLogManageService;
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
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端链上提现审核（基础 pass/reject，不含三方打款）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletToWebLogManageServiceImpl implements WalletToWebLogManageService {

	@Autowired
	private WalletToWebLogDao walletToWebLogDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private WalletAccountDao walletAccountDao;

	@Override
	@SysLogAnnotation(module = "链上提现", type = "POST", remark = "提现列表")
	public ResponseBase findList(@RequestBody(required = false) WalletToWebLogEntity entity) {
		if (entity == null) {
			entity = new WalletToWebLogEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletToWebLogEntity> list = walletToWebLogDao.findList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletToWebLogEntity row : list) {
			row.setApplyStateName(WalletRecordStateEnums.labelOf(row.getApplyState()));
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "链上提现", type = "GET", remark = "审核通过")
	public ResponseBase pass(Long id, String gooleCode) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		if (StringUtils.isEmpty(gooleCode)) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		WalletToWebLogEntity entity = walletToWebLogDao.selectById(id);
		if (entity == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		// 基础实现：标记处理中；三方打款需对接 MonitorUtil 后扩展
		entity.setApplyState(WalletRecordStateEnums.AUDIT_PASS_PROCESSING.getCode());
		entity.setApplyStateName(WalletRecordStateEnums.AUDIT_PASS_PROCESSING.getLabel());
		entity.setGmtModified(new Date());
		try {
			walletToWebLogDao.updateById(entity);
			WalletLogEntity logEntity = walletLogDao.findByOutOrderNo(entity.getOrderNo());
			if (logEntity != null) {
				logEntity.setStatus(WalletLogStatusEnums.PROCESSING.getCode());
				logEntity.setGmtModified(new Date());
				walletLogDao.updateById(logEntity);
			}
		} catch (Exception e) {
			log.error("walletToWebLog pass failed id={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("walletToWebLog pass id={} gooleCodePresent=true", id);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "链上提现", type = "GET", remark = "审核拒绝")
	public ResponseBase reject(Long id, String rejectContent) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		WalletToWebLogEntity entity = walletToWebLogDao.selectById(id);
		if (entity == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		entity.setApplyState(WalletRecordStateEnums.AUDIT_FAIL.getCode());
		entity.setApplyStateName(WalletRecordStateEnums.AUDIT_FAIL.getLabel());
		entity.setRejectContent(rejectContent);
		entity.setGmtModified(new Date());
		try {
			walletToWebLogDao.updateById(entity);
			WalletLogEntity logEntity = walletLogDao.findByOutOrderNo(entity.getOrderNo());
			if (logEntity != null) {
				logEntity.setStatus(WalletLogStatusEnums.FAILED.getCode());
				logEntity.setGmtModified(new Date());
				walletLogDao.updateById(logEntity);
			}
			// 退回冻结金额到可用余额
			if (entity.getWalletUserId() != null && entity.getTransferAmount() != null) {
				WalletAccountEntity account = walletAccountDao.findByWalletUserId(entity.getWalletUserId());
				if (account != null) {
					walletAccountDao.addAvailableBalance(account.getId(), entity.getTransferAmount());
				}
			}
		} catch (Exception e) {
			log.error("walletToWebLog reject failed id={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("walletToWebLog reject id={}", id);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}
}
