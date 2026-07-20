package com.playlet.internal.query.drama;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CommentGiveLikeQuery {

	@NotNull(message = "评论、回复id不能为空")
	@ApiModelProperty(name = "commentId",value = "评论、回复id",required = true,dataType = "Integer")
	private Integer commentId;
	
	@NotNull(message = "用户id不能为空")
	@ApiModelProperty(name = "userId",value = "用户id",required = true,dataType = "Integer")
	private Integer userId;
	
	@NotNull(message = "操作类型不能为空")
	@ApiModelProperty(name = "operationType",value = "操作类型 1点赞2取消点赞",required = true,dataType = "Integer")
	private Integer operationType;
	
}
