package com.playlet.internal.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 创作者金币流水业务类型。
 * <p>
 * biz_type 为历史已落库的字符串值，不能改名；这里只补充「展示分类 / 标题」映射。
 */
public enum CreatorCoinBizTypeEnums {

	/** 剧本收益（征稿奖金、定制剧本收入等） */
	PLAY_INCOME("PLAY_INCOME", "收入", "剧本收益"),
	/** 分账收益 */
	SHARE("SHARE", "分账", "剧本分账收益"),
	/** 平台服务费扣除 */
	SERVICE_FEE("SERVICE_FEE", "支出", "平台服务费扣除"),
	/** 提现申请 */
	WITHDRAW("WITHDRAW", "提现", "提现申请"),
	/** 提现失败退回 */
	WITHDRAW_REFUND("WITHDRAW_REFUND", "提现", "提现退回"),
	/** 系统调账 */
	SYSTEM("SYSTEM", "支出", "系统调账");

	private final String bizType;
	private final String category;
	private final String title;

	CreatorCoinBizTypeEnums(String bizType, String category, String title) {
		this.bizType = bizType;
		this.category = category;
		this.title = title;
	}

	public String getBizType() {
		return bizType;
	}

	public String getCategory() {
		return category;
	}

	public String getTitle() {
		return title;
	}

	public static CreatorCoinBizTypeEnums ofBizType(String bizType) {
		if (bizType == null) {
			return null;
		}
		for (CreatorCoinBizTypeEnums item : values()) {
			if (item.bizType.equalsIgnoreCase(bizType)) {
				return item;
			}
		}
		return null;
	}

	/** 展示标题，未知类型兜底为 bizType 本身 */
	public static String titleOf(String bizType) {
		CreatorCoinBizTypeEnums item = ofBizType(bizType);
		return item == null ? (bizType == null ? "" : bizType) : item.title;
	}

	/** 展示分类（收入/支出/分账/提现），未知类型按金额方向兜底 */
	public static String categoryOf(String bizType, Long changeAmt) {
		CreatorCoinBizTypeEnums item = ofBizType(bizType);
		if (item != null) {
			return item.category;
		}
		return changeAmt != null && changeAmt > 0 ? "收入" : "支出";
	}

	/**
	 * 前端筛选项（收入/支出/分账/提现）→ biz_type 集合；返回 null 表示「全部」不过滤。
	 */
	public static List<String> bizTypesOfCategory(String category) {
		if (category == null || category.trim().isEmpty() || "全部".equals(category.trim())) {
			return null;
		}
		String target = category.trim();
		List<String> result = new ArrayList<>();
		for (CreatorCoinBizTypeEnums item : values()) {
			if (item.category.equals(target)) {
				result.add(item.bizType);
			}
		}
		return result.isEmpty() ? Collections.singletonList("__NONE__") : result;
	}

	/** 流水筛选项字典 */
	public static List<String> categories() {
		return Arrays.asList("全部", "收入", "支出", "分账", "提现");
	}
}
