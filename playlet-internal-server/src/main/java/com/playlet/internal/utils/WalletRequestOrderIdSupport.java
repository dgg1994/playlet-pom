package com.playlet.internal.utils;

/**
 * 钱包业务 requestOrderId 解析：客户端可传幂等单号，不传则由服务端按前缀自动生成。
 */
public final class WalletRequestOrderIdSupport {

	private static final long DEFAULT_SEED = 10000L;

	private WalletRequestOrderIdSupport() {
	}

	/**
	 * @param requestOrderId 客户端传入的幂等单号，可为空
	 * @param prefix         业务前缀，如 CA / CR / FT
	 * @param userKey        参与编码的用户标识（localUid / walletUid 等）
	 */
	public static String resolve(String requestOrderId, String prefix, Long userKey) {
		if (!StringUtils.isEmpty(requestOrderId)) {
			return requestOrderId.trim();
		}
		long seed = userKey == null ? DEFAULT_SEED : userKey;
		String safePrefix = prefix == null ? "" : prefix;
		return safePrefix + OrderCodeFactory.getOrderCode(seed);
	}
}
