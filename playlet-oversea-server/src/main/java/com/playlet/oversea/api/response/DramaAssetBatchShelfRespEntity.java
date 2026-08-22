package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 集批量上架/下架结果。
 */
@Data
@ApiModel(value = "集批量上下架结果", description = "成功数 + 失败明细")
public class DramaAssetBatchShelfRespEntity {

	@ApiModelProperty("成功数量")
	private Integer successCount;

	@ApiModelProperty("失败数量")
	private Integer failCount;

	@ApiModelProperty("成功集ID")
	private List<Integer> successIds = new ArrayList<>();

	@ApiModelProperty("失败明细")
	private List<DramaAssetBatchShelfFailItem> failItems = new ArrayList<>();
}
