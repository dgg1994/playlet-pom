package com.playlet.internal.enums;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * 认证身份编码（creator_certification.identity_types 内的 1-4）。
 * <p>注意：与 {@link CreatorIdentityTypeEnums}（个人/机构/工作室）不是同一维度。</p>
 */
public enum CreatorCertIdentityEnums {

	SCREENWRITER(1, "编剧", "编剧机构"),
	ACTOR(2, "演员", "演员机构"),
	DIRECTOR(3, "导演", "导演机构"),
	COPYRIGHT(4, "版权人", "版权机构");

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final int code;
	/** 个人创作者身份名称 */
	private final String personalName;
	/** 机构创作者身份名称 */
	private final String orgName;

	CreatorCertIdentityEnums(int code, String personalName, String orgName) {
		this.code = code;
		this.personalName = personalName;
		this.orgName = orgName;
	}

	public int getCode() {
		return code;
	}

	public String nameOf(Integer category) {
		return category != null && category == 2 ? orgName : personalName;
	}

	/**
	 * 把 identity_types（JSON 数字数组）转成「编剧/演员」文案。
	 *
	 * @param category 1 个人 / 2 机构
	 * @param json     形如 [1,2] 的 JSON 数组
	 */
	public static String namesOf(Integer category, String json) {
		if (json == null || json.trim().isEmpty()) {
			return "";
		}
		try {
			List<Integer> codes = MAPPER.readValue(json, MAPPER.getTypeFactory()
					.constructCollectionType(List.class, Integer.class));
			StringBuilder sb = new StringBuilder();
			for (Integer code : codes) {
				for (CreatorCertIdentityEnums item : values()) {
					if (item.code == code) {
						if (sb.length() > 0) {
							sb.append("/");
						}
						sb.append(item.nameOf(category));
					}
				}
			}
			return sb.toString();
		} catch (Exception e) {
			return "";
		}
	}
}
