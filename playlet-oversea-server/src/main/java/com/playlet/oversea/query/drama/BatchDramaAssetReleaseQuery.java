package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 登记/同步剧集请求（POST /dramaAsset/release）。
 */
@Data
@ApiModel(value = "登记同步剧集", description = "批量登记；历史集 assetId+setNum；新集 setNum+key")
public class BatchDramaAssetReleaseQuery {

	@NotNull(message = "短剧id不能为空")
	@ApiModelProperty(value = "剧ID", required = true)
	private Integer dramaId;

	@NotEmpty(message = "剧集列表不能为空")
	@Valid
	@ApiModelProperty(value = "剧集列表", required = true)
	private List<BatchDramaAssetEpisodeItemQuery> episodes;
}
