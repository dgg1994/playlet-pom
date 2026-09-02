package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.wallet.WalletCardCloseDao;
import com.playlet.internal.entity.wallet.WalletCardCloseEntity;
import com.playlet.internal.service.CardCloseManageService;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端销卡记录列表。
 */
@Slf4j
@RestController
@CrossOrigin
public class CardCloseManageServiceImpl implements CardCloseManageService {

	private static final String REVIEW_PROCESSING = "审核中";
	private static final String REVIEW_SUCCESS = "审核成功";
	private static final String REVIEW_FAIL = "审核失败";

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
			row.setReviewStatusName(resolveReviewName(row.getReviewStatus()));
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	private static String resolveReviewName(Integer status) {
		if (status == null) {
			return null;
		}
		if (status == 1) {
			return REVIEW_PROCESSING;
		}
		if (status == 2) {
			return REVIEW_SUCCESS;
		}
		if (status == 3) {
			return REVIEW_FAIL;
		}
		return String.valueOf(status);
	}
}
