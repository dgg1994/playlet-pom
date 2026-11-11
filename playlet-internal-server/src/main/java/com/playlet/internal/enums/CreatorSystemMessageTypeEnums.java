package com.playlet.internal.enums;

/**
 * 作家站内信类型。
 */
public enum CreatorSystemMessageTypeEnums {
	AUDIT("AUDIT", "评审通知"),
	SITE("SITE", "系统公告消息"),
	COOP("COOP", "合作消息"),
	SUBMIT("SUBMIT", "征稿投稿消息"),
	CONTRACT("CONTRACT", "合同消息"),
	INCOME("INCOME", "收益结算通知");

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
	 * 类型对应的页面分类名，用于消息列表展示。
	 */
	public static String displayName(String code) {
		if (code == null) {
			return "";
		}
		for (CreatorSystemMessageTypeEnums item : values()) {
			if (item.code.equals(code)) {
				return item.desc;
			}
		}
		return "";
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
		if (lang == LanguageEnums.EN_US) {
			return "Site Notice";
		}
		if (lang == LanguageEnums.ZH_TW) {
			return "站務通知";
		}
		return desc;
	}
}
