package com.playlet.internal.service.support;

import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserWithdrawOrderDao;
import com.playlet.internal.enums.OnePayBindStatusEnums;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * C 端账号表上的 OnePay 绑定。
 */
@Component
public class AppOnePayBindOps implements OnePayBindOps {

	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;

	@Override
	public void bind(Integer uid, String onepayAccount, String openid, Date bindTime) {
		appAccountDao.updateOnePayBind(uid, onepayAccount, openid,
				OnePayBindStatusEnums.BOUND.getCode(), bindTime);
	}

	@Override
	public void unbind(Integer uid) {
		appAccountDao.clearOnePayBind(uid, OnePayBindStatusEnums.UNBOUND.getCode());
	}

	@Override
	public int countProcessingWithdraw(Integer uid) {
		return userWithdrawOrderDao.countProcessingByUid(uid, WithdrawUserTypeEnums.APP.getCode());
	}
}
