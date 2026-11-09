package com.playlet.internal.constants;

/**
 * 剧本市场常量。
 */
public final class ScriptConstants {

	private ScriptConstants() {
	}

	/** 语言 */
	public static final String LANGUE = "zh-cn";
	/** 题材分组 */
	public static final String TAG_GROUP_TOPIC = "topic";
	/** 元素分组 */
	public static final String TAG_GROUP_ELEMENT = "element";
	/** 题材全部 */
	public static final String TOPIC_ALL = "all";

	/** 排序：综合 */
	public static final int SORT_COMPREHENSIVE = 1;
	/** 排序：最新 */
	public static final int SORT_LATEST = 2;
	/** 排序：评分 */
	public static final int SORT_SCORE = 3;
	/** 排序：价格 */
	public static final int SORT_PRICE = 4;
	/** 排序：热度 */
	public static final int SORT_HEAT = 5;

	/** 默认推荐条数 */
	public static final int DEFAULT_LIMIT = 4;
	/** 推荐条数上限 */
	public static final int MAX_LIMIT = 10;

	/** 申请过期天数 */
	public static final int APPLY_EXPIRE_DAYS = 7;

	/** 操作人类型：作家 */
	public static final int OPERATOR_CREATOR = 1;
	/** 操作人类型：管理员 */
	public static final int OPERATOR_ADMIN = 2;

	/** 日志动作：审核 */
	public static final int LOG_ACTION_AUDIT = 1;
	/** 日志动作：上下架 */
	public static final int LOG_ACTION_SHELF = 2;
	/** 日志动作：创建 */
	public static final int LOG_ACTION_CREATE = 3;
	/** 日志动作：编辑 */
	public static final int LOG_ACTION_EDIT = 4;

	/** 处理动作：同意 */
	public static final int HANDLE_AGREE = 1;
	/** 处理动作：拒绝 */
	public static final int HANDLE_REJECT = 2;

	/** 题材兜底选项 */
	public static final String[] TOPIC_OPTIONS = {
			"都市", "古风", "民国", "乡村", "悬疑", "甜宠", "逆袭", "豪门", "玄幻"
	};
}
