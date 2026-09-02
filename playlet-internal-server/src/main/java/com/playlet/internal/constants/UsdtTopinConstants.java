package com.playlet.internal.constants;

/**
 * USDT 链上监听网关常量。
 */
public final class UsdtTopinConstants {

	private UsdtTopinConstants() {
	}

	public static final int SUCCESS_CODE = 200;

	public static final String HEADER_API_KEY = "APIKEY";

	public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

	/** 回调类型：链上转入 */
	public static final String NOTIFY_TYPE_IN = "in";

	public static final String COIN_USDT = "USDT";

	public static final String COIN_USDC = "USDC";
}
