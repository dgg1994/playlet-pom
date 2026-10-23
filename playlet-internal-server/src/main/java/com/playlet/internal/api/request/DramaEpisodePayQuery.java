package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短剧付费-剧集付费明细分页入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "剧集付费明细查询", description = "按短剧查付费集统计分页")
public class DramaEpisodePayQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "短剧ID", required = true)
	private Integer dramaId;
}
