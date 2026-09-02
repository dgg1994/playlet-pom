package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.wallet.WalletCardSynopsisDao;
import com.playlet.internal.dao.wallet.WalletCardSynopsisJoinDao;
import com.playlet.internal.entity.wallet.WalletCardSynopsisEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.CardSynopsisManageService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端卡简介 CRUD。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CardSynopsisManageServiceImpl implements CardSynopsisManageService {

	@Autowired
	private WalletCardSynopsisDao walletCardSynopsisDao;
	@Autowired
	private WalletCardSynopsisJoinDao walletCardSynopsisJoinDao;

	@Override
	@SysLogAnnotation(module = "卡简介管理", type = "POST", remark = "新增简介")
	public ResponseBase add(@RequestBody WalletCardSynopsisEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getLanguage())
				|| StringUtils.isEmpty(entity.getTitle()) || StringUtils.isEmpty(entity.getContent())) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		Date now = new Date();
		entity.setSetTime(now);
		entity.setGmtModified(now);
		try {
			walletCardSynopsisDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card synopsis add failed", e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡简介管理", type = "POST", remark = "编辑简介")
	public ResponseBase update(@RequestBody WalletCardSynopsisEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		entity.setGmtModified(new Date());
		try {
			walletCardSynopsisDao.updateById(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card synopsis update failed id={}", entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡简介管理", type = "GET", remark = "删除简介")
	public ResponseBase delete(Integer id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		try {
			walletCardSynopsisJoinDao.deleteBySynopsisId(id);
			walletCardSynopsisDao.deleteById(id);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card synopsis delete failed id={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡简介管理", type = "POST", remark = "简介列表")
	public ResponseBase findList(@RequestBody(required = false) WalletCardSynopsisEntity entity) {
		if (entity == null) {
			entity = new WalletCardSynopsisEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletCardSynopsisEntity> list = walletCardSynopsisDao.findList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletCardSynopsisEntity row : list) {
			row.setLanguageName(row.getLanguage());
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "卡简介管理", type = "GET", remark = "全部简介")
	public ResponseBase findAll() {
		List<WalletCardSynopsisEntity> list = walletCardSynopsisDao.findList(new WalletCardSynopsisEntity());
		return setResultSuccess(list == null ? new ArrayList<>() : list, I18nUtil.getMessage("base_success"));
	}
}
