package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletTollAddressDao;
import com.playlet.oversea.entity.wallet.WalletTollAddressEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.TollAddressManageService;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端收款地址 CRUD。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class TollAddressManageServiceImpl implements TollAddressManageService {

	@Autowired
	private WalletTollAddressDao walletTollAddressDao;

	@Override
	@SysLogAnnotation(module = "收款地址", type = "POST", remark = "地址列表")
	public ResponseBase findList(@RequestBody(required = false) WalletTollAddressEntity entity) {
		if (entity == null) {
			entity = new WalletTollAddressEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletTollAddressEntity> list = walletTollAddressDao.findList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "收款地址", type = "POST", remark = "新增地址")
	public ResponseBase add(@RequestBody WalletTollAddressEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getAddressType())
				|| StringUtils.isEmpty(entity.getAddressSite())) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		if (walletTollAddressDao.findByType(entity.getAddressType()) != null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Date now = new Date();
		entity.setSetTime(now);
		entity.setGmtModified(now);
		try {
			walletTollAddressDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("toll address add failed type={}", entity.getAddressType(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "收款地址", type = "POST", remark = "编辑地址")
	public ResponseBase update(@RequestBody WalletTollAddressEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		entity.setGmtModified(new Date());
		try {
			walletTollAddressDao.updateById(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("toll address update failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "收款地址", type = "GET", remark = "删除地址")
	public ResponseBase delete(Integer id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		try {
			walletTollAddressDao.deleteById(id);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("toll address delete failed id={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}
}
