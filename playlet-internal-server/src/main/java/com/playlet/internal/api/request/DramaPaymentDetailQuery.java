package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 短剧付费详情入参。
 */
@Data
@ApiModel(value = "短剧付费详情查询", description = "按短剧ID查详情汇总")
public class DramaPaymentDetailQuery {

	@ApiModelProperty(value = "短剧ID", required = true)
	private Integer dramaId;

	@ApiModelProperty("剧集付费明细返回条数，默认 10")
	private Integer episodeLimit;
}
