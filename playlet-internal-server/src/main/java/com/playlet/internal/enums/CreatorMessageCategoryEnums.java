package com.playlet.internal.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 作家端消息中心分类（页面 Tab）。
 * value 为前端传参，types 为该分类下对应的消息类型；null 表示不限类型。
 */
public enum CreatorMessageCategoryEnums {

	ALL("ALL", "全部消息", null),
	SYSTEM("SYSTEM", "系统公告消息", Collections.singletonList(CreatorSystemMessageTypeEnums.SITE.getCode())),
	AUDIT("AUDIT", "审核消息", Collections.singletonList(CreatorSystemMessageTypeEnums.AUDIT.getCode())),
	COOP("COOP", "合作消息", Collections.singletonList(CreatorSystemMessageTypeEnums.COOP.getCode())),
	SUBMIT("SUBMIT", "征稿投稿消息", Collections.singletonList(CreatorSystemMessageTypeEnums.SUBMIT.getCode())),
	CONTRACT("CONTRACT", "合同消息", Collections.singletonList(CreatorSystemMessageTypeEnums.CONTRACT.getCode())),
	INCOME("INCOME", "收益结算通知", Collections.singletonList(CreatorSystemMessageTypeEnums.INCOME.getCode()));

	private final String code;
	private final String desc;
	private final List<String> types;

	CreatorMessageCategoryEnums(String code, String desc, List<String> types) {
		this.code = code;
		this.desc = desc;
		this.types = types;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	public List<String> getTypes() {
		return types;
	}

	/**
	 * 解析分类，未知或为空按全部处理。
	 */
	public static CreatorMessageCategoryEnums of(String code) {
		if (code == null || code.trim().isEmpty()) {
			return ALL;
		}
		String target = code.trim().toUpperCase();
		return Arrays.stream(values())
				.filter(item -> item.code.equals(target))
				.findFirst()
				.orElse(ALL);
	}

	/**
	 * 分类对应的消息类型；全部返回 null 表示不限。
	 */
	public static List<String> typesOf(String code) {
		return of(code).getTypes();
	}
}
