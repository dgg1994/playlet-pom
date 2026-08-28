package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.response.WithdrawOrderAdminItemEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WithdrawConstants;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.enums.WithdrawOrderStatusEnums;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.query.welfare.WithdrawOrderAdminQuery;
import com.playlet.internal.service.WithdrawOrderManageService;
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
import java.util.List;

import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 财务管理：用户/作家提现记录分页（表格列：流水ID/名称/金币/折合/支付方式/账户）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WithdrawOrderManageServiceImpl implements WithdrawOrderManageService {

	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;

	@Override
	@SysLogAnnotation(module = "提现订单管理", type = "POST", remark = "用户提现列表")
	public ResponseBase findUserList(@RequestBody(required = false) WithdrawOrderAdminQuery query) {
		return findList(query, WithdrawUserTypeEnums.APP);
	}

	@Override
	@SysLogAnnotation(module = "提现订单管理", type = "POST", remark = "作家提现列表")
	public ResponseBase findCreatorList(@RequestBody(required = false) WithdrawOrderAdminQuery query) {
		return findList(query, WithdrawUserTypeEnums.CREATOR);
	}

	/** 分页列表；补支付方式与状态文案后直接返回 PageInfo。 */
	private ResponseBase findList(WithdrawOrderAdminQuery query, WithdrawUserTypeEnums userType) {
		if (query == null) {
			query = new WithdrawOrderAdminQuery();
		}
		normalizeProcessFlag(query);
		int typeCode = userType.getCode();
		PageHelper.startPage(query.getPageNumber(), query.getPageSize());
		List<WithdrawOrderAdminItemEntity> list;
		if (userType == WithdrawUserTypeEnums.CREATOR) {
			list = userWithdrawOrderDao.findAdminCreatorList(query, typeCode);
		} else {
			list = userWithdrawOrderDao.findAdminAppList(query, typeCode);
		}
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WithdrawOrderAdminItemEntity item : list) {
			fillDisplayFields(item);
		}
		PageInfo<WithdrawOrderAdminItemEntity> pageInfo = new PageInfo<>(list);
		log.info("withdraw order admin list userType={} processFlag={} total={}",
				typeCode, query.getProcessFlag(), pageInfo.getTotal());
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	/** 支付方式展示文案 + 状态文案。 */
	private void fillDisplayFields(WithdrawOrderAdminItemEntity item) {
		item.setPayMethod(resolvePayMethod(item.getAssetCode()));
		item.setStatusLabel(WithdrawOrderStatusEnums.getLableByCode(item.getStatus()));
		if (item.getWithdrawCoin() == null) {
			item.setWithdrawCoin(0);
		}
		if (item.getCurrencyAmt() == null) {
			item.setCurrencyAmt(BigDecimal.ZERO);
		}
	}

	private static String resolvePayMethod(String assetCode) {
		if (StringUtils.isEmpty(assetCode)) {
			return WithdrawConstants.PAY_METHOD_WALLET_LABEL;
		}
		if (WithdrawConstants.ASSET_WALLET.equalsIgnoreCase(assetCode)) {
			return WithdrawConstants.PAY_METHOD_WALLET_LABEL;
		}
		return assetCode;
	}

	private void normalizeProcessFlag(WithdrawOrderAdminQuery query) {
		Integer flag = query.getProcessFlag();
		if (flag == null) {
			return;
		}
		boolean valid = flag == WithdrawConstants.PROCESS_FLAG_UNPROCESSED
				|| flag == WithdrawConstants.PROCESS_FLAG_PROCESSED;
		if (!valid) {
			query.setProcessFlag(null);
		}
	}
}
