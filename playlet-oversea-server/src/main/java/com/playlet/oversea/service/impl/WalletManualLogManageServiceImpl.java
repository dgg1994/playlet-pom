package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletManualLogDao;
import com.playlet.oversea.entity.wallet.WalletManualLogEntity;
import com.playlet.oversea.service.WalletManualLogManageService;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端人工充值日志列表。
 */
@Slf4j
@RestController
@CrossOrigin
public class WalletManualLogManageServiceImpl implements WalletManualLogManageService {

	@Autowired
	private WalletManualLogDao walletManualLogDao;

	@Override
	@SysLogAnnotation(module = "人工充值日志", type = "POST", remark = "日志列表")
	public ResponseBase findList(@RequestBody(required = false) WalletManualLogEntity entity) {
		if (entity == null) {
			entity = new WalletManualLogEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletManualLogEntity> list = walletManualLogDao.findList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}
}
