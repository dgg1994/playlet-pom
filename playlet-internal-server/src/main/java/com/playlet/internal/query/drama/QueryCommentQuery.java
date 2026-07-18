package com.playlet.internal.query.drama;

import javax.validation.constraints.NotNull;

import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryCommentQuery extends PageQueryHelperEntity{

	@NotNull(message = "视频id不能为空")
	@ApiModelProperty(name = "voideId",value = "视频id",required = true,dataType = "Integer")
	private Integer voideId;
	
	
}
