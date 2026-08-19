package com.playlet.internal.service.support;

import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按提现主体路由钱包 Handler。
 */
@Component
public class WithdrawWalletHandlerRegistry {

	private final Map<Integer, WithdrawWalletHandler> handlers = new HashMap<>();

	@Autowired
	public WithdrawWalletHandlerRegistry(List<WithdrawWalletHandler> list) {
		if (list == null) {
			return;
		}
		for (WithdrawWalletHandler handler : list) {
			handlers.put(handler.userType().getCode(), handler);
		}
	}

	public WithdrawWalletHandler of(Integer userType) {
		WithdrawWalletHandler handler = handlers.get(WithdrawUserTypeEnums.fromCode(userType).getCode());
		if (handler == null) {
			throw new BaseException("unsupported withdraw user type");
		}
		return handler;
	}
}
