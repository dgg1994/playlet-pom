package com.playlet.oversea.service.impl;

import com.playlet.oversea.api.request.*;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;

import java.math.BigDecimal;
import com.playlet.oversea.service.WithdrawService;
import com.playlet.oversea.service.support.WalletCardholderService;
import com.playlet.oversea.service.support.WalletTransferService;
import com.playlet.oversea.service.support.WalletUsdtTopinService;
import com.playlet.oversea.service.support.WithdrawBizService;
import com.playlet.oversea.service.third.WalletUserService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	public ResponseBase topinUsdtNotify(@RequestBody UsdtTopinNotifyRequest query, HttpServletRequest request) {
		log.info("usdt topin callback hash={} uid={}", query == null ? null : query.getHash(),
				query == null ? null : query.getUid());
		return walletUsdtTopinService.handleNotify(query, request);
	}

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
		return withdrawBizService.submit(uid, query, WithdrawUserTypeEnums.APP);
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
	public ResponseBase checkPayPwd(@RequestBody WalletCheckPayPwdRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		String payPassword = query == null ? null : query.getPayPassword();
		return walletUserService.checkPayPasswordMatch(WithdrawUserTypeEnums.APP.getCode(), uid, payPassword);
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
	public ResponseBase kycApplyByCardApply(Long applyId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.applyKycByCardApply(WithdrawUserTypeEnums.APP.getCode(), uid, applyId);
	}

	@Override
	public ResponseBase kycFileUpload(MultipartFile idCard,
			Integer certType, Integer documentType, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.uploadKycFile(WithdrawUserTypeEnums.APP.getCode(), uid,
				idCard, certType, documentType);
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
	public ResponseBase findUserCardInfo(Long id, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findUserCardInfo(WithdrawUserTypeEnums.APP.getCode(), uid, id);
	}

	@Override
	public ResponseBase upCardTag(@RequestBody WalletCardTagRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.upCardTag(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardFindList(WalletCardProductListRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findCardProductList(query);
	}

	@Override
	public ResponseBase cardProductDetail(Integer productId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.getCardProductDetail(productId);
	}

	@Override
	public ResponseBase findLogistics(String logisticsNum, Long applyId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findLogistics(WithdrawUserTypeEnums.APP.getCode(), uid, logisticsNum, applyId);
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
	public ResponseBase cardholderAdd(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.add(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardholderUpdate(@RequestBody WalletCardholderSaveRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.update(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase cardholderDelete(Long id, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.delete(WithdrawUserTypeEnums.APP.getCode(), uid, id);
	}

	@Override
	public ResponseBase cardholderFindByUid(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.list(WithdrawUserTypeEnums.APP.getCode(), uid);
	}

	@Override
	public ResponseBase cardholderFindById(Long id, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletCardholderService.findById(WithdrawUserTypeEnums.APP.getCode(), uid, id);
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
	public ResponseBase cardTopUp(@RequestBody BankcardRechargeRequest query, HttpServletRequest request) {
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
	public ResponseBase cardClose(@RequestBody BankcardCloseRequest query, HttpServletRequest request) {
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
	public ResponseBase transactionList(PageQueryHelperEntity page, Long userBankcardId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listTransactions(WithdrawUserTypeEnums.APP.getCode(), uid, page, userBankcardId);
	}

	@Override
	public ResponseBase transfer(@RequestBody WalletTransfetListEntity query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletTransferService.transfer(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase transferReading(Double sendMoney, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		BigDecimal amount = sendMoney == null ? null : BigDecimal.valueOf(sendMoney);
		return walletTransferService.transferReading(WithdrawUserTypeEnums.APP.getCode(), uid, amount);
	}

	@Override
	public ResponseBase findReading(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletTransferService.findReading();
	}

	@Override
	public ResponseBase walletLog(@RequestBody(required = false) WalletLogEntity query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletTransferService.walletLog(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase mailingRegion(@RequestBody(required = false) WalletMailingRegionRequest query,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.listMailingRegions(query);
	}

	@Override
	public ResponseBase mailingAdd(@RequestBody WalletMailingAddressAddRequest query, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.addMailingAddress(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase mailingUpdate(@RequestBody WalletMailingAddressUpdateRequest query,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.updateMailingAddress(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}

	@Override
	public ResponseBase mailingFind(@RequestBody(required = false) WalletMailingAddressFindRequest query,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.findMailingAddresses(WithdrawUserTypeEnums.APP.getCode(), uid, query);
	}
}
