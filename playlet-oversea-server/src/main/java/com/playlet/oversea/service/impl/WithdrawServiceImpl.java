package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.request.OnePayWithdrawCallbackRequest;
import com.playlet.oversea.api.request.WithdrawReqEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.constants.WithdrawConstants;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import com.playlet.oversea.service.WithdrawPayoutService;
import com.playlet.oversea.service.WithdrawService;
import com.playlet.oversea.service.support.WithdrawBizService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
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
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
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
