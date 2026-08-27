package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.WalletBindPayPwdRequest;
import com.playlet.internal.api.request.WithdrawReqEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.CreatorWithdrawService;
import com.playlet.internal.service.support.CreatorRevenueBizService;
import com.playlet.internal.service.support.WithdrawBizService;
import com.playlet.internal.service.third.WalletUserService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端钱包提现入口。
 */
@RestController
@CrossOrigin
public class CreatorWithdrawServiceImpl extends BaseApiService implements CreatorWithdrawService {

	@Autowired
	private WithdrawBizService withdrawBizService;
	@Autowired
	private CreatorRevenueBizService creatorRevenueBizService;
	@Autowired
	private WalletUserService walletUserService;

	@Override
	public ResponseBase revenueSummary(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return setResultSuccess(creatorRevenueBizService.buildSummary(uid), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase withdrawHome(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return withdrawBizService.home(uid, WithdrawUserTypeEnums.CREATOR);
	}

	@Override
	public ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Integer points = query == null ? null : query.getPoints();
		return withdrawBizService.submit(uid, points, WithdrawUserTypeEnums.CREATOR);
	}

	@Override
	public ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return withdrawBizService.records(page, uid, WithdrawUserTypeEnums.CREATOR);
	}

	@Override
	public ResponseBase fundRecords(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return setResultSuccess(creatorRevenueBizService.fundRecords(uid, page), I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase bindPayPwd(@RequestBody WalletBindPayPwdRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.bindPayPassword(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardList(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listCards(WithdrawUserTypeEnums.CREATOR.getCode(), uid);
	}

	@Override
	public ResponseBase transactionList(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listTransactions(WithdrawUserTypeEnums.CREATOR.getCode(), uid, page);
	}
}
