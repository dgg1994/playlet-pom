package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.api.request.WalletCardProductUpdateRequest;
import com.playlet.oversea.api.response.WalletCardProductSyncResp;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletCardProductDao;
import com.playlet.oversea.entity.wallet.WalletCardProductEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.WalletCardProductManageService;
import com.playlet.oversea.service.support.WalletCardProductService;
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
 * U 卡产品管理：列表 / 三方同步 / 本地维护。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletCardProductManageServiceImpl implements WalletCardProductManageService {

	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletCardProductService walletCardProductService;

	@Override
	@SysLogAnnotation(module = "U卡产品管理", type = "POST", remark = "产品列表")
	public ResponseBase findList(@RequestBody(required = false) WalletCardProductEntity entity) {
		if (entity == null) {
			entity = new WalletCardProductEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletCardProductEntity> list = walletCardProductDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		// 补齐标签列表、卡简介，供管理端编辑回显
		walletCardProductService.enrichAdminDisplay(list);
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "U卡产品管理", type = "GET", remark = "卡产品详情")
	public ResponseBase findById(Integer productId) {
		try {
			WalletCardProductEntity detail = walletCardProductService.findByProductId(productId);
			return setResultSuccess(detail, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card product findById failed productId={}", productId, e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card product findById error productId={}", productId, e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	@Override
	@SysLogAnnotation(module = "U卡产品管理", type = "POST", remark = "同步三方卡产品")
	public ResponseBase syncFromThird() {
		try {
			WalletCardProductSyncResp result = walletCardProductService.syncFromThird();
			return setResultSuccess(result, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card product sync failed", e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card product sync error", e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	@Override
	@SysLogAnnotation(module = "U卡产品管理", type = "POST", remark = "维护卡产品")
	public ResponseBase update(@RequestBody WalletCardProductUpdateRequest entity) {
		try {
			walletCardProductService.updateLocalFields(entity);
			return setResultSuccess(null, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("wallet card product update failed id={}", entity == null ? null : entity.getId(), e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("wallet card product update error id={}", entity == null ? null : entity.getId(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}
}
