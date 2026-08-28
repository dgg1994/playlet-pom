package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.BankcardActiveRequest;
import com.playlet.internal.api.request.BankcardCanActiveRequest;
import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.api.request.BankcardSetPinRequest;
import com.playlet.internal.api.request.BankcardUpdateEmailRequest;
import com.playlet.internal.api.request.BankcardUpdateStatusRequest;
import com.playlet.internal.api.request.BankcardUserIdRequest;
import com.playlet.internal.api.request.KycApplyRequest;
import com.playlet.internal.api.request.KycCountryListRequest;
import com.playlet.internal.api.request.OnePayWithdrawCallbackRequest;
import com.playlet.internal.api.request.WalletApplyCardRequest;
import com.playlet.internal.api.request.WalletBindPayPwdRequest;
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
import com.playlet.internal.service.third.WalletUserService;
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
	@Autowired
	private WalletUserService walletUserService;

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

	@Override
	public ResponseBase bindPayPwd(@RequestBody WalletBindPayPwdRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.bindPayPassword(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase kycCountryList(@RequestBody(required = false) KycCountryListRequest query,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		String name = query == null ? null : query.getName();
		return walletUserService.listKycCountries(name);
	}

	@Override
	public ResponseBase kycStatus(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getKycStatus(WithdrawUserTypeEnums.APP.getCode(), uid);
	}

	@Override
	public ResponseBase kycApply(@RequestBody KycApplyRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.applyKyc(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardList(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listCards(WithdrawUserTypeEnums.APP.getCode(), uid);
	}

	@Override
	public ResponseBase cardProductList(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listCardProducts();
	}

	@Override
	public ResponseBase applyCard(@RequestBody WalletApplyCardRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.applyCard(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardCanActive(@RequestBody BankcardCanActiveRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.canActiveCard(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardActive(@RequestBody BankcardActiveRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.activeCard(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardSetPin(@RequestBody BankcardSetPinRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.setCardPin(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardBalance(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getCardBalance(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardRecharge(@RequestBody BankcardRechargeRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.rechargeCard(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardUpdateStatus(@RequestBody BankcardUpdateStatusRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.updateCardStatus(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardClose(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.closeCard(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardInfo(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getCardInfo(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardUpdateEmail(@RequestBody BankcardUpdateEmailRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.updateCardEmail(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardQueryPin(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.queryCardPin(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase transactionList(PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listTransactions(WithdrawUserTypeEnums.APP.getCode(), uid, page);
	}
}
