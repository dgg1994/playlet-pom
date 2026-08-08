package com.playlet.internal.query.drama;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddDramaAssetQuery {
	
	@NotNull(message = "短剧id不能为空")
	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "String")
	private Integer dramaId;
	
	@NotNull(message = "当前集数不能为空")
	@ApiModelProperty(name = "setNum",value = "第几集",required = true,dataType = "Integer")
	private Integer setNum;
	
	@ApiModelProperty(name = "remarkInfo",value = "备注",required = false,dataType = "String")
	private String remarkInfo;

	@NotBlank(message = "视频 key 不能为空")
	@ApiModelProperty(name = "key", value = "七牛对象 key（前端直传完成后回传）", required = true, dataType = "String")
	private String key;

	@ApiModelProperty(name = "videoName", value = "原始文件名", required = false, dataType = "String")
	private String videoName;

}
