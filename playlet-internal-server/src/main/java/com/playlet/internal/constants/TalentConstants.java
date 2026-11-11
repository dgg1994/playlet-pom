package com.playlet.internal.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 人才市场常量。
 */
public class TalentConstants {

	/** 市场角色：编剧 */
	public static final int ROLE_WRITER = 1;
	/** 市场角色：创作者（制作团队） */
	public static final int ROLE_CREATOR = 2;

	/** 排序：综合排序 */
	public static final int SORT_COMPREHENSIVE = 1;
	/** 排序：成交最多 */
	public static final int SORT_DEALS = 2;
	/** 排序：好评最高 */
	public static final int SORT_SCORE = 3;
	/** 排序：价格最低 */
	public static final int SORT_PRICE_ASC = 4;
	/** 排序：作品最多 */
	public static final int SORT_WORKS = 5;
	/** 排序：累计收益最高 */
	public static final int SORT_REVENUE = 6;

	/** 筛选项"全部" */
	public static final String FILTER_ALL = "全部";

	/** 资质类型：个人 */
	public static final int QUAL_PERSONAL = 1;
	/** 资质类型：公司 */
	public static final int QUAL_COMPANY = 2;
	/** 资质类型：团队 */
	public static final int QUAL_TEAM = 3;

	private static final Map<String, Integer> QUAL_LABEL_TO_CODE = new HashMap<>();
	private static final Map<Integer, String> QUAL_CODE_TO_LABEL = new HashMap<>();

	static {
		QUAL_LABEL_TO_CODE.put("个人", QUAL_PERSONAL);
		QUAL_LABEL_TO_CODE.put("公司", QUAL_COMPANY);
		QUAL_LABEL_TO_CODE.put("团队", QUAL_TEAM);
		QUAL_CODE_TO_LABEL.put(QUAL_PERSONAL, "个人");
		QUAL_CODE_TO_LABEL.put(QUAL_COMPANY, "公司");
		QUAL_CODE_TO_LABEL.put(QUAL_TEAM, "团队");
	}

	/** 资质类型文案转编码，无法识别返回 null */
	public static Integer qualCodeOf(String label) {
		if (label == null || label.trim().isEmpty() || FILTER_ALL.equals(label.trim())) {
			return null;
		}
		return QUAL_LABEL_TO_CODE.get(label.trim());
	}

	/** 资质类型编码转文案 */
	public static String qualLabelOf(Integer code) {
		if (code == null) {
			return "";
		}
		return QUAL_CODE_TO_LABEL.getOrDefault(code, "");
	}

	/** 接单状态：可接单 */
	public static final int ACCEPT_OPEN = 1;
	/** 接单状态：排期中 */
	public static final int ACCEPT_BUSY = 2;

	/** 接单状态文案 */
	public static String acceptLabelOf(Integer status) {
		if (status != null && status == ACCEPT_BUSY) {
			return "排期中";
		}
		return "可接单";
	}
}
