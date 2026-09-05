package com.playlet.internal.service.support;

import com.playlet.internal.entity.wallet.WalletUserEntity;
import com.playlet.internal.enums.WalletNotifyEventEnums;

/**
 * 钱包系统消息 + 极光推送（App 收件箱+推送；作家写站内信，极光按设备能力尽力推送）。
 */
public interface WalletNotifyService {

	/**
	 * 按本地主体推送；幂等 bizId。
	 */
	void notify(Integer userType, Integer localUid, WalletNotifyEventEnums event,
			String bizId, String jumpType, String jumpParam, Object... contentArgs);

	/**
	 * 按钱包用户推送。
	 */
	void notify(WalletUserEntity user, WalletNotifyEventEnums event,
			String bizId, String jumpType, String jumpParam, Object... contentArgs);
}
