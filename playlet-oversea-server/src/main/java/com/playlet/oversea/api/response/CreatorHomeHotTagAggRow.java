package com.playlet.oversea.api.response;

import lombok.Data;

/**
 * 作家首页热点题材聚合行（Dao 映射）。
 */
@Data
public class CreatorHomeHotTagAggRow {

	private String groupId;
	private String tagName;
	private Long hitCnt;
}
