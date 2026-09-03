package com.playlet.oversea.service.impl;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import com.playlet.oversea.service.WalletFileUploadService;
import com.playlet.oversea.service.third.WalletUserService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import static com.playlet.oversea.base.BaseApiService.setResultError;

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
