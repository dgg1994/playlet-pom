package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.request.WithdrawReqEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import com.playlet.oversea.service.CreatorWithdrawService;
import com.playlet.oversea.service.support.CreatorRevenueBizService;
import com.playlet.oversea.service.support.WithdrawBizService;
import com.playlet.oversea.utils.CreatorTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
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
}
