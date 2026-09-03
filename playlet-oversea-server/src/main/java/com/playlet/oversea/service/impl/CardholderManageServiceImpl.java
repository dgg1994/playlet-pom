package com.playlet.oversea.service.impl;

import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.WalletConstants;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.dao.wallet.WalletUserHolderDao;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.service.CardholderManageService;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端持卡人查询。
 */
@Slf4j
@RestController
@CrossOrigin
public class CardholderManageServiceImpl implements CardholderManageService {

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletUserHolderDao walletUserHolderDao;

	@Override
	@SysLogAnnotation(module = "持卡人管理", type = "GET", remark = "按uid查持卡人")
	public ResponseBase findByUid(String uid) {
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
		return setResultSuccess(walletUserHolderDao.findByWalletUserId(user.getId()),
				I18nUtil.getMessage("base_success"));
	}
}
