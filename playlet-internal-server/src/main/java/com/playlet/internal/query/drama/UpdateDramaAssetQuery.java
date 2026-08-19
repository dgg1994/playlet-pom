package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 剧集修改请求：驳回后可改集序/视频/备注并重新进审。
 */
@Data
@ApiModel(value = "剧集修改", description = "驳回后重传剧集视频并重置审核")
public class UpdateDramaAssetQuery {

	@NotNull(message = "剧集ID不能为空")
	@ApiModelProperty(name = "id", value = "剧集ID", required = true, dataType = "Integer")
	private Integer id;

	@NotNull(message = "短剧id不能为空")
	@ApiModelProperty(name = "dramaId", value = "剧ID", required = true, dataType = "String")
	private Integer dramaId;

	@NotNull(message = "当前集数不能为空")
	@ApiModelProperty(name = "setNum", value = "第几集", required = true, dataType = "Integer")
	private Integer setNum;

	@ApiModelProperty(name = "remarkInfo", value = "备注", required = false, dataType = "String")
	private String remarkInfo;

	@NotBlank(message = "视频 key 不能为空")
	@ApiModelProperty(name = "key", value = "七牛对象 key（前端直传完成后回传）", required = true, dataType = "String")
	private String key;

	@ApiModelProperty(name = "videoName", value = "原始文件名", required = false, dataType = "String")
	private String videoName;
}
