package com.playlet.internal.constants;

/**
 * USDT 链上充值三方接口路径。
 */
public final class UsdtTopinApiPaths {

	private UsdtTopinApiPaths() {
	}

	/** 创建/获取 Web3 充值地址（对齐 worldpay UsdtTopinMethods.getAddressurl） */
	public static final String GET_ADDRESS = "/listen/getaddress";

	/** 旧版路径，已废弃 */
	public static final String CREATE_ACCOUNT = GET_ADDRESS;

	/** Telegram 通知（多链人工审核等，预留） */
	public static final String MSG_NOTIFY = "/v1/sendtotg";
}
