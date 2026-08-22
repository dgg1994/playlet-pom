package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.OnePayWithdrawCallbackRequest;
import com.playlet.internal.api.request.WithdrawReqEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.WithdrawConstants;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.WithdrawPayoutService;
import com.playlet.internal.service.WithdrawService;
import com.playlet.internal.service.support.WithdrawBizService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端钱包提现入口。
 */
@Slf4j
@RestController
@CrossOrigin
public class WithdrawServiceImpl extends BaseApiService implements WithdrawService {

	@Autowired
	private WithdrawBizService withdrawBizService;
	@Autowired
	private WithdrawPayoutService withdrawPayoutService;

	@Override
	public ResponseBase withdrawHome(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		return withdrawBizService.home(uid, WithdrawUserTypeEnums.APP);
	}

	@Override
	public ResponseBase withdraw(@RequestBody WithdrawReqEntity query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Integer points = query == null ? null : query.getPoints();
		return withdrawBizService.submit(uid, points, WithdrawUserTypeEnums.APP);
	}

	@Override
	public ResponseBase onepayCallback(@RequestBody OnePayWithdrawCallbackRequest query) {
		if (query == null || StringUtils.isEmpty(query.getOrderNo())
				|| query.getSuccess() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		boolean success = query.getSuccess() == WithdrawConstants.CALLBACK_SUCCESS;
		log.info("onepay withdraw callback orderNo={} success={}", query.getOrderNo(), query.getSuccess());
		withdrawPayoutService.handleCallback(query.getOrderNo(), success,
				query.getThirdOrderNo(), query.getFailReason());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase withdrawRecords(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return withdrawBizService.records(page, uid, WithdrawUserTypeEnums.APP);
	}
}
