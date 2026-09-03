package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletCardLabelDao;
import com.playlet.oversea.dao.wallet.WalletCardLabelJoinDao;
import com.playlet.oversea.entity.wallet.WalletCardLabelEntity;
import com.playlet.oversea.enums.LanguageEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.WalletCardLabelManageService;
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
 * U 卡标签管理：新增 / 删除 / 列表（对齐 onetoken CardLableServiceImpl）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletCardLabelManageServiceImpl implements WalletCardLabelManageService {

	@Autowired
	private WalletCardLabelDao walletCardLabelDao;
	@Autowired
	private WalletCardLabelJoinDao walletCardLabelJoinDao;

	@Override
	@SysLogAnnotation(module = "银行卡标签", type = "POST", remark = "新增标签")
	public ResponseBase add(@RequestBody WalletCardLabelEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getName()) || StringUtils.isEmpty(entity.getLanguage())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletCardLabelEntity existed = walletCardLabelDao.findByName(entity.getName().trim());
		if (existed != null) {
			return setResultError(I18nUtil.getMessage("lable_check"));
		}
		entity.setName(entity.getName().trim());
		entity.setLanguage(entity.getLanguage().trim());
		Date now = new Date();
		entity.setSetTime(now);
		entity.setGmtModified(now);
		try {
			walletCardLabelDao.insert(entity);
		} catch (Exception e) {
			log.error("wallet card label add failed name={}", entity.getName(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet card label add success id={} name={}", entity.getId(), entity.getName());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡标签", type = "GET", remark = "删除标签")
	public ResponseBase delete(Integer id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			walletCardLabelDao.deleteById(id);
			walletCardLabelJoinDao.deleteByLabelId(id);
		} catch (Exception e) {
			log.error("wallet card label delete failed id={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet card label delete success id={}", id);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡标签", type = "POST", remark = "标签列表")
	public ResponseBase findList(@RequestBody(required = false) WalletCardLabelEntity entity) {
		if (entity == null) {
			entity = new WalletCardLabelEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletCardLabelEntity> list = walletCardLabelDao.findList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		fillLanguageName(list);
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "银行卡标签", type = "GET", remark = "全部标签")
	public ResponseBase findAll() {
		List<WalletCardLabelEntity> list = walletCardLabelDao.selectList(null);
		if (list == null) {
			list = new ArrayList<>();
		}
		fillLanguageName(list);
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	private static void fillLanguageName(List<WalletCardLabelEntity> list) {
		for (WalletCardLabelEntity row : list) {
			if (row == null || StringUtils.isEmpty(row.getLanguage())) {
				continue;
			}
			row.setLanguageName(LanguageEnums.getLable(row.getLanguage()));
		}
	}
}
