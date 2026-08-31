package com.playlet.internal.service.impl;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.service.WalletFileUploadService;
import com.playlet.internal.service.third.WalletUserService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import static com.playlet.internal.base.BaseApiService.setResultError;

/**
 * KYC 证件文件上传（C 端）。
 */
@RestController
@CrossOrigin
public class WalletFileUploadServiceImpl implements WalletFileUploadService {

	@Autowired
	private WalletUserService walletUserService;

	@Override
	public ResponseBase upload(MultipartFile idCard, Integer certType, Integer documentType,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		return walletUserService.uploadKycFile(WithdrawUserTypeEnums.APP.getCode(), uid,
				idCard, certType, documentType);
	}
}
