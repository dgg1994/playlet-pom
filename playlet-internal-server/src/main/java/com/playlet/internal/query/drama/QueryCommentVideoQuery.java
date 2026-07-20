package com.playlet.internal.query.drama;

import javax.validation.constraints.NotNull;

import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryCommentVideoQuery extends PageQueryHelperEntity{

	@NotNull(message = "视频id不能为空")
	@ApiModelProperty(name = "voideId",value = "视频id",required = true,dataType = "Integer")
	private Integer voideId;
	
	@ApiModelProperty(name = "deleteState",value = "删除状态",required = true,dataType = "Integer")
	private Integer deleteState;
	
	@ApiModelProperty(name = "parentId",value = "父级id",required = true,dataType = "Integer")
	private Integer parentId;
}
