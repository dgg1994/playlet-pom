package com.playlet.internal.api.response;

import lombok.Data;

/**
 * 作家首页榜单聚合行（Dao 映射）。
 */
@Data
public class CreatorHomeRankAggRow {

	private Integer creatorId;
	private String nickname;
	private Long score;
}
