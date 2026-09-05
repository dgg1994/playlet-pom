package com.playlet.oversea.service.support;

import java.util.Date;

/**
 * OnePay 绑定落库（作家资料）。
 */
public interface OnePayBindOps {

	void bind(Integer uid, String onepayAccount, String openid, Date bindTime);

	void unbind(Integer uid);

	int countProcessingWithdraw(Integer uid);
}
