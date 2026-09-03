package com.playlet.oversea.service.impl;

import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletCardApplyDao;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.entity.wallet.WalletCardApplyEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.service.KycManageService;
import com.playlet.oversea.service.third.WalletUserService;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static com.playlet.oversea.base.BaseApiService.setResultError;

/**
 * 管理端 KYC：按开卡申请代提交三方 KYC。
 */
@Slf4j
@RestController
@CrossOrigin
public class KycManageServiceImpl implements KycManageService {

	@Autowired
	private WalletCardApplyDao walletCardApplyDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletUserService walletUserService;

	@Override
	@SysLogAnnotation(module = "KYC管理", type = "GET", remark = "提交KYC")
	public ResponseBase apply(Long applyId) {
		if (applyId == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		WalletCardApplyEntity apply = walletCardApplyDao.selectById(applyId);
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("wallet.apply_not_found"));
		}
		WalletUserEntity user = walletUserDao.selectById(apply.getWalletUserId());
		if (user == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		return walletUserService.applyKycByCardApply(user.getUserType(), user.getLocalUid(), applyId);
	}
}
