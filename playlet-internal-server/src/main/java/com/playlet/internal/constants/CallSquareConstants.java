package com.playlet.internal.constants;

/**
 * 征稿广场常量。
 */
public class CallSquareConstants {

	/** 筛选项"全部" */
	public static final String FILTER_ALL = "全部";

	/** 征稿状态：征稿中 */
	public static final int STATUS_OPEN = 1;
	/** 征稿状态：即将截止 */
	public static final int STATUS_SOON = 2;
	/** 征稿状态：已结束 */
	public static final int STATUS_ENDED = 3;

	/** 即将截止判定天数：截止日期距今 <= 15 天 */
	public static final int SOON_DAYS = 15;

	/** 排序：综合排序 */
	public static final int SORT_COMPREHENSIVE = 1;
	/** 排序：最新发布 */
	public static final int SORT_LATEST = 2;
	/** 排序：奖金最高 */
	public static final int SORT_AMOUNT = 3;
	/** 排序：参与最多 */
	public static final int SORT_PARTICIPANTS = 4;
	/** 排序：即将截止 */
	public static final int SORT_DEADLINE = 5;

	/** 状态编码转文案 */
	public static String statusLabelOf(Integer status) {
		if (status == null) {
			return "";
		}
		switch (status) {
			case STATUS_SOON:
				return "即将截止";
			case STATUS_ENDED:
				return "已结束";
			default:
				return "征稿中";
		}
	}

	/** 是否独家文案 */
	public static String licenseModeOf(Integer exclusive) {
		return exclusive != null && exclusive == 1 ? "独家授权" : "非独家授权";
	}

	/** 授权说明文案 */
	public static String licenseNoteOf(Integer exclusive) {
		return exclusive != null && exclusive == 1 ? "买断模式，一经采用不再售卖" : "可采用分成模式，作者保留改编权";
	}

	/** 归一化文本筛选值：全部/空串视为不筛 */
	public static String normalizeText(String text) {
		if (text == null) {
			return null;
		}
		String trimmed = text.trim();
		if (trimmed.isEmpty() || FILTER_ALL.equals(trimmed)) {
			return null;
		}
		return trimmed;
	}

	// ======= 发布征稿默认值 =======

	/** 发布方兜底昵称（前端不传 publisher 时使用） */
	public static final String DEFAULT_PUBLISHER = "创作者";
	/** 参与数初始值（DB 层已有 DEFAULT 0，这里仅作为代码层兜底） */
	public static final int DEFAULT_PARTICIPANTS = 0;
	/** 新发布默认下架，等审核通过后再上架，避免脏数据污染列表 */
	public static final int DEFAULT_SHELF_STATUS_OFF = 0;
	public static final int DEFAULT_DELETE_STATE_NORMAL = 0;
	/** 新发布默认综合权重 */
	public static final int DEFAULT_SORT_WEIGHT = 10;
	/** 默认空 JSON 数组字符串，tags/attachments/submissions 无值时统一使用 */
	public static final String EMPTY_JSON_ARRAY = "[]";

	/** 参与征稿已结束的错误提示 */
	public static final String ERROR_ENDED_JOIN = "该征稿已结束，无法参与";
	/** 发布成功但进入审核的提示 */
	public static final String PUBLISH_SUCCESS_MSG = "发布成功，进入审核流程，通过后将对外展示";

	// ======= 参与征稿（投稿）默认值与提示 =======

	/** 投稿兜底昵称（账号昵称为空时使用） */
	public static final String DEFAULT_SUBMITTER = "创作者";
	/** 请求体为空错误提示 */
	public static final String ERROR_EMPTY_BODY = "请求体不能为空";
	/** 投稿已结束错误提示 */
	public static final String ERROR_ENDED_SUBMIT = "该征稿已结束，无法投稿";
	/** 重复投稿错误提示 */
	public static final String ERROR_REPEAT_SUBMIT = "您已投稿该征稿，请勿重复提交";
	/** 投稿成功提示 */
	public static final String SUBMIT_SUCCESS_MSG = "投稿成功，请在消息通知中查看选稿进度";
}
