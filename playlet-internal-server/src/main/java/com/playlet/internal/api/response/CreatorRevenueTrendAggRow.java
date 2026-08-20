package com.playlet.internal.api.response;

import lombok.Data;

/**
 * 作家收益按日聚合行（Dao 映射）。
 */
@Data
public class CreatorRevenueTrendAggRow {

	private String bizDate;
	private Long incomeCoin;
}
