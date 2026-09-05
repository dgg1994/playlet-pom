package com.playlet.internal.enums;

/**
 * 作家站内信类型。
 */
public enum CreatorSystemMessageTypeEnums {
	AUDIT("AUDIT", "评审通知"),
	SITE("SITE", "站务通知"),
	/** 钱包交易 / 卡操作等 */
	WALLET("WALLET", "钱包");

	private final String code;
	private final String desc;

	CreatorSystemMessageTypeEnums(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	/**
	 * 写入收件箱时的卡片标题（快照）。
	 */
	public String title(String langue) {
		LanguageEnums lang = LanguageEnums.of(langue);
		if (this == AUDIT) {
			if (lang == LanguageEnums.EN_US) {
				return "Review Notice";
			}
			if (lang == LanguageEnums.ZH_TW) {
				return "評審通知";
			}
			return desc;
		}
		if (this == WALLET) {
			if (lang == LanguageEnums.EN_US) {
				return "Wallet";
			}
			if (lang == LanguageEnums.ZH_TW) {
				return "錢包";
			}
			return desc;
		}
		if (lang == LanguageEnums.EN_US) {
			return "Site Notice";
		}
		if (lang == LanguageEnums.ZH_TW) {
			return "站務通知";
		}
		return desc;
	}
}
