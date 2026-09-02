package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.response.AppUserInfoReqEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.wallet.WalletKycFileDao;
import com.playlet.internal.dao.wallet.WalletUserDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.wallet.WalletKycFileEntity;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletKycStateEnums;
import com.playlet.internal.service.WalletAppUserManageService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端 APP 用户列表与 KYC 文件。
 */
@Slf4j
@RestController
@CrossOrigin
public class WalletAppUserManageServiceImpl implements WalletAppUserManageService {

	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletKycFileDao walletKycFileDao;

	@Override
	@SysLogAnnotation(module = "APP用户管理", type = "POST", remark = "用户列表")
	public ResponseBase findList(@RequestBody(required = false) AppAccountEntity entity) {
		if (entity == null) {
			entity = new AppAccountEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<AppUserInfoReqEntity> list = appAccountDao.findWalletAppUserList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (AppUserInfoReqEntity row : list) {
			if (row.getKycState() != null) {
				row.setKycStateName(WalletKycStateEnums.fromCode(row.getKycState()).getLabel());
			}
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "APP用户管理", type = "GET", remark = "KYC文件")
	public ResponseBase findKycFile(String uid) {
		if (StringUtils.isEmpty(uid)) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		Integer localUid;
		try {
			localUid = Integer.parseInt(uid.trim());
		} catch (NumberFormatException e) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		WalletUserEntity user = walletUserDao.findByLocal(WalletConstants.USER_TYPE_APP, localUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		List<WalletKycFileEntity> files = walletKycFileDao.findByWalletUserId(user.getId());
		Map<String, Object> resp = new HashMap<>();
		if (files != null) {
			for (WalletKycFileEntity file : files) {
				if (file.getDocumentType() == null) {
					continue;
				}
				if (file.getDocumentType() == WalletConstants.KYC_DOC_FRONT) {
					resp.put("frontPhotoId", file.getDocumentFileId());
					resp.put("frontPhotoUrl", file.getDocumentFileUrl());
				} else if (file.getDocumentType() == WalletConstants.KYC_DOC_BACK) {
					resp.put("backPhotoId", file.getDocumentFileId());
					resp.put("backPhotoUrl", file.getDocumentFileUrl());
				} else if (file.getDocumentType() == WalletConstants.KYC_DOC_HANDHELD) {
					resp.put("handheldPhotoId", file.getDocumentFileId());
					resp.put("handheldPhotoUrl", file.getDocumentFileUrl());
				}
			}
		}
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}
}
