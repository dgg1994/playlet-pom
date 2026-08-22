package com.playlet.oversea.service.support;

import com.playlet.oversea.dao.account.AppAccountDao;
import com.playlet.oversea.dao.welfare.UserWithdrawOrderDao;
import com.playlet.oversea.enums.OnePayBindStatusEnums;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
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
