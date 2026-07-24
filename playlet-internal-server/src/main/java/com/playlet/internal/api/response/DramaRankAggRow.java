package com.playlet.internal.api.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 算法榜候选行（聚合结果）
 */
@Data
public class DramaRankAggRow {

	private Integer dramaId;
	private String dramaTitle;
	private String coverUrl;
	private String hotScoreText;
	private Integer totalEpisodes;
	private Integer finishedState;

	private Long validSeconds;
	private Long collectCnt;
	private Long likeCnt;
	private Long playPv;

	/** 计算后的排序分 */
	private BigDecimal algoScore;
}
