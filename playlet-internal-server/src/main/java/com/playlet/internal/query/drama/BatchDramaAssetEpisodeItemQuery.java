package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 批量登记剧集条目：历史集可仅传 assetId+setNum；新上传须传 key。
 */
@Data
@ApiModel(value = "批量登记剧集条目", description = "历史集 assetId+setNum；新集 setNum+key")
public class BatchDramaAssetEpisodeItemQuery {

	@ApiModelProperty(value = "已有剧集ID；历史数据必填，新上传不传")
	private Integer assetId;

	@NotNull(message = "集数不能为空")
	@ApiModelProperty(value = "第几集", required = true)
	private Integer setNum;

	@ApiModelProperty(value = "七牛对象 key（新上传或驳回重传时传；历史集仅改序可不传）")
	private String key;

	@ApiModelProperty(value = "原始文件名")
	private String videoName;

	@ApiModelProperty(value = "备注")
	private String remarkInfo;
}
