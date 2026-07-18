package com.playlet.internal.query.drama;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddDramaCommentQuery {
	
	@NotNull(message = "剧ID不能为空")
	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "Integer")
	private Integer dramaId;
	
	@NotNull(message = "视频id不能为空")
	@ApiModelProperty(name = "videoId",value = "视频id",required = true,dataType = "Integer")
	private Integer videoId;
	
	@NotNull(message = "评论用户ID不能为空")
	@ApiModelProperty(name = "userId",value = "评论用户ID",required = true,dataType = "Integer")
	private Integer userId;
	
	@NotBlank(message = "评论用户名不能为空")
	@ApiModelProperty(name = "userName",value = "评论用户名",required = true,dataType = "String")
	private String userName;

	@NotBlank(message = "评论内容不能为空")
	@ApiModelProperty(name = "commentInfo",value = "评论内容",required = true,dataType = "String")
	private String commentInfo;

}
