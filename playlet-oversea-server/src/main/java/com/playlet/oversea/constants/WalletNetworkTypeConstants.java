package com.playlet.oversea.constants;

/**
 * Web3 充值网络展示名（对齐 worldpay NetworkTypeEnums）。
 */
public final class WalletNetworkTypeConstants {

	private WalletNetworkTypeConstants() {
	}

	public static final String TRON = "TRON";

	public static final String BSC = "BSC";

	/** EVM 地址前缀（BSC/ETH） */
	private static final String EVM_ADDRESS_PREFIX = "0x";

	/**
	 * 优先回调 chain；为空时按地址前缀推断（T→TRON，0x→BSC）。
	 */
	public static String resolve(String chain, String... addresses) {
		if (chain != null) {
			String trimmed = chain.trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
		}
		if (addresses == null) {
			return null;
		}
		for (String address : addresses) {
			String inferred = inferFromAddress(address);
			if (inferred != null) {
				return inferred;
			}
		}
		return null;
	}

	/** 从钱包地址推断网络类型（历史脏数据展示兜底）。 */
	public static String inferFromAddress(String address) {
		if (address == null) {
			return null;
		}
		String trimmed = address.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		// 0x… → BSC（EVM）
		if (trimmed.regionMatches(true, 0, EVM_ADDRESS_PREFIX, 0, EVM_ADDRESS_PREFIX.length())) {
			return BSC;
		}
		// T… → TRON
		char first = trimmed.charAt(0);
		if (first == 'T' || first == 't') {
			return TRON;
		}
		return null;
	}
}
