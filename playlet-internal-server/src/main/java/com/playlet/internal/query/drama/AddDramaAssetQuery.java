package com.playlet.internal.query.drama;

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

}
