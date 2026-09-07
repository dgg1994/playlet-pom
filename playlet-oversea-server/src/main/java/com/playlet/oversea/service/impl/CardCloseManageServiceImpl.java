package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletCardCloseDao;
import com.playlet.oversea.entity.wallet.WalletCardCloseEntity;
import com.playlet.oversea.enums.WalletCardCloseReviewStatusEnums;
import com.playlet.oversea.service.CardCloseManageService;
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
 * 管理端销卡记录列表。
 */
@Slf4j
@RestController
@CrossOrigin
public class CardCloseManageServiceImpl implements CardCloseManageService {

	@Autowired
	private WalletCardCloseDao walletCardCloseDao;

	@Override
	@SysLogAnnotation(module = "销卡申请", type = "POST", remark = "销卡列表")
	public ResponseBase findList(@RequestBody(required = false) WalletCardCloseEntity entity) {
		if (entity == null) {
			entity = new WalletCardCloseEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletCardCloseEntity> list = walletCardCloseDao.findList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletCardCloseEntity row : list) {
			row.setReviewStatusName(WalletCardCloseReviewStatusEnums.getName(row.getReviewStatus()));
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}
}
