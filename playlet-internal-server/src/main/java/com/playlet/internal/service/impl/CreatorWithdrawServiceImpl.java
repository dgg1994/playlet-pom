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
import com.playlet.internal.api.request.WalletApplyCardRequest;
import com.playlet.internal.api.request.WalletBindPayPwdRequest;
import com.playlet.internal.api.request.WalletCardholderSaveRequest;
import com.playlet.internal.api.request.WithdrawReqEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.CreatorWithdrawService;
import com.playlet.internal.service.support.CreatorRevenueBizService;
import com.playlet.internal.service.support.WalletCardholderService;
import com.playlet.internal.service.support.WalletUsdtTopinService;
import com.playlet.internal.service.support.WithdrawBizService;
import com.playlet.internal.service.third.WalletUserService;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	@Autowired
	private WalletUsdtTopinService walletUsdtTopinService;
	@Autowired
	private WalletCardholderService walletCardholderService;

	@Override
	public ResponseBase topinUsdtAddress(String uid) {
		return walletUsdtTopinService.getTopinAddress(uid);
	}

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
		return withdrawBizService.submit(uid, query, WithdrawUserTypeEnums.CREATOR);
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
	public ResponseBase kycCountryList(@RequestBody(required = false) KycCountryListRequest query,
			HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		String name = query == null ? null : query.getName();
		return walletUserService.listKycCountries(name);
	}

	@Override
	public ResponseBase kycStatus(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getKycStatus(WithdrawUserTypeEnums.CREATOR.getCode(), uid);
	}

	@Override
	public ResponseBase kycApply(@RequestBody KycApplyRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.applyKyc(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase kycApplyByCardApply(Long applyId, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.applyKycByCardApply(WithdrawUserTypeEnums.CREATOR.getCode(), uid, applyId);
	}

	@Override
	public ResponseBase kycFileUpload(MultipartFile idCard,
			Integer certType, Integer documentType, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.uploadKycFile(WithdrawUserTypeEnums.CREATOR.getCode(), uid,
				idCard, certType, documentType);
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
	public ResponseBase cardProductList(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listCardProducts();
	}

	@Override
	public ResponseBase cardProductDetail(Integer productId, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getCardProductDetail(productId);
	}

	@Override
	public ResponseBase applyCard(@RequestBody WalletApplyCardRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.applyCard(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardholderAdd(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.add(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardholderUpdate(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.update(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardholderDelete(Long id, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.delete(WithdrawUserTypeEnums.CREATOR.getCode(), uid, id);
	}

	@Override
	public ResponseBase cardholderFindByUid(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.list(WithdrawUserTypeEnums.CREATOR.getCode(), uid);
	}

	@Override
	public ResponseBase cardholderFindById(Long id, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.findById(WithdrawUserTypeEnums.CREATOR.getCode(), uid, id);
	}

	@Override
	public ResponseBase cardCanActive(@RequestBody BankcardCanActiveRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.canActiveCard(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardActive(@RequestBody BankcardActiveRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.activeCard(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardSetPin(@RequestBody BankcardSetPinRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.setCardPin(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardBalance(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getCardBalance(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardRecharge(@RequestBody BankcardRechargeRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.rechargeCard(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardUpdateStatus(@RequestBody BankcardUpdateStatusRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.updateCardStatus(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardClose(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.closeCard(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardInfo(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getCardInfo(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardUpdateEmail(@RequestBody BankcardUpdateEmailRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.updateCardEmail(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardQueryPin(@RequestBody BankcardUserIdRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.queryCardPin(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
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
