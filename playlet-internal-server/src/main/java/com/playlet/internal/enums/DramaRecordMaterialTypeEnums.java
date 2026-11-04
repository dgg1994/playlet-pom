package com.playlet.internal.enums;

/**
 * 备案材料类型。
 */
public enum DramaRecordMaterialTypeEnums {
	BUSINESS_LICENSE(1, "营业执照（盖章扫描件）", 1),
	CREATOR_ID_CARD(2, "创作者身份证（正反面）", 1),
	COPYRIGHT_PROMISE(3, "内容合规及版权承诺书", 1),
	SCRIPT_AUTH(4, "剧本授权文件", 2),
	ACTOR_PORTRAIT_AUTH(5, "演员肖像权授权协议", 2),
	BGM_AUTH(6, "BGM/字体/图片素材授权文件", 2),
	SHORT_DRAMA_LICENSE(7, "微短剧发行许可证", 3),
	RECORD_PUBLIC_DOC(8, "备案公示文件", 3),
	SPECIAL_THEME(9, "特殊题材材料", 3),
	OPENING_TRAILER(10, "片头片尾审核", 4),
	COMIC_AUTH(11, "漫画/动漫改编授权", 2),
	OUTLINE_PDF(12, "分集大纲文件", 5),
	CHARACTER_PDF(13, "人物小传文件", 5),
	PRODUCTION_LICENSE(14, "制作经营许可证", 3),
	OTHER(15, "其他材料", 2),
	ARTIST_COPYRIGHT(16, "画师著作权声明", 2),
	COMIC_CHARACTER_AUTH(17, "漫画人物形象授权", 2),
	PROJECT_CONTRACT(18, "关联项目合同", 2);

	private final int code;
	private final String label;
	/** 材料分组：1主体信息 2版权材料 3备案材料 4片头片尾 5剧本材料 */
	private final int group;

	DramaRecordMaterialTypeEnums(int code, String label, int group) {
		this.code = code;
		this.label = label;
		this.group = group;
	}

	public int getCode() {
		return code;
	}

	public String getLabel() {
		return label;
	}

	public int getGroup() {
		return group;
	}

	public static DramaRecordMaterialTypeEnums fromCode(Integer code) {
		if (code == null) return null;
		for (DramaRecordMaterialTypeEnums item : values()) {
			if (item.code == code) return item;
		}
		return null;
	}
}
