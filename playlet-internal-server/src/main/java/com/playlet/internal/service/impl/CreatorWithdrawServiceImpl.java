package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.*;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.entity.wallet.WalletLogEntity;
import com.playlet.internal.entity.wallet.WalletTransfetListEntity;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.CreatorWithdrawService;
import com.playlet.internal.service.support.CreatorRevenueBizService;
import com.playlet.internal.service.support.WalletCardholderService;
import com.playlet.internal.service.support.WalletTransferService;
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

import java.math.BigDecimal;

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
	@Autowired
	private WalletTransferService walletTransferService;

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
	public ResponseBase findUserCardInfo(Long id, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findUserCardInfo(WithdrawUserTypeEnums.CREATOR.getCode(), uid, id);
	}

	@Override
	public ResponseBase upCardTag(@RequestBody WalletCardTagRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.upCardTag(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardFindList(WalletCardProductListRequest query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findCardProductList(query);
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
	public ResponseBase findLogistics(String logisticsNum, Long applyId, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findLogistics(WithdrawUserTypeEnums.CREATOR.getCode(), uid, logisticsNum, applyId);
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
	public ResponseBase cardTopUp(@RequestBody BankcardRechargeRequest query, HttpServletRequest request) {
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
	public ResponseBase cardClose(@RequestBody BankcardCloseRequest query, HttpServletRequest request) {
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
	public ResponseBase transactionList(PageQueryHelperEntity page, Long userBankcardId, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listTransactions(WithdrawUserTypeEnums.CREATOR.getCode(), uid, page, userBankcardId);
	}

	@Override
	public ResponseBase transfer(@RequestBody WalletTransfetListEntity query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletTransferService.transfer(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}

	@Override
	public ResponseBase transferReading(Double sendMoney, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		BigDecimal amount = sendMoney == null ? null : BigDecimal.valueOf(sendMoney);
		return walletTransferService.transferReading(WithdrawUserTypeEnums.CREATOR.getCode(), uid, amount);
	}

	@Override
	public ResponseBase findReading(HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletTransferService.findReading();
	}

	@Override
	public ResponseBase walletLog(@RequestBody(required = false) WalletLogEntity query, HttpServletRequest request) {
		Integer uid = CreatorTokenUtil.resolveCreatorId(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletTransferService.walletLog(WithdrawUserTypeEnums.CREATOR.getCode(), uid, query);
	}
}
