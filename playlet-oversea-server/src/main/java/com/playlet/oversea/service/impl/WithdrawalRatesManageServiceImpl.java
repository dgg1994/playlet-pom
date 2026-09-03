package com.playlet.oversea.service.impl;

import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletWithdrawalRatesDao;
import com.playlet.oversea.entity.wallet.WalletWithdrawalRatesEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.WithdrawalRatesManageService;
import com.playlet.oversea.utils.I18nUtil;
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

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端提现费率。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WithdrawalRatesManageServiceImpl implements WithdrawalRatesManageService {

	private static final BigDecimal DEFAULT_MIN = BigDecimal.valueOf(10);
	private static final BigDecimal DEFAULT_MAX = BigDecimal.valueOf(10000);
	private static final BigDecimal DEFAULT_RATE = BigDecimal.valueOf(0.01);
	private static final BigDecimal DEFAULT_SERVER = BigDecimal.valueOf(1);

	@Autowired
	private WalletWithdrawalRatesDao walletWithdrawalRatesDao;

	@Override
	@SysLogAnnotation(module = "提现费率", type = "GET", remark = "查询费率")
	public ResponseBase find() {
		List<WalletWithdrawalRatesEntity> list = walletWithdrawalRatesDao.findAll();
		if (list == null || list.isEmpty()) {
			WalletWithdrawalRatesEntity defaults = new WalletWithdrawalRatesEntity();
			defaults.setMinAmount(DEFAULT_MIN);
			defaults.setMaxAmount(DEFAULT_MAX);
			defaults.setHandlingRates(DEFAULT_RATE);
			defaults.setServerAmount(DEFAULT_SERVER);
			list = new ArrayList<>();
			list.add(defaults);
		}
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "提现费率", type = "POST", remark = "更新费率")
	public ResponseBase update(@RequestBody WalletWithdrawalRatesEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		entity.setGmtModified(new Date());
		try {
			walletWithdrawalRatesDao.updateById(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("withdrawal rates update failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}
}
