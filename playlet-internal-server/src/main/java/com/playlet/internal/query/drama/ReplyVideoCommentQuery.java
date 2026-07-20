package com.playlet.internal.query.drama;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ReplyVideoCommentQuery {

	@NotNull(message = "剧ID不能为空")
	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "Integer")
	private Integer dramaId;
	
	@NotNull(message = "视频id不能为空")
	@ApiModelProperty(name = "videoId",value = "视频id",required = true,dataType = "Integer")
	private Integer videoId;
	
	@NotNull(message = "回复用户ID不能为空")
	@ApiModelProperty(name = "userId",value = "评论用户ID",required = true,dataType = "Integer")
	private Integer userId;
	
	@NotBlank(message = "回复用户名不能为空")
	@ApiModelProperty(name = "userName",value = "评论用户名",required = true,dataType = "String")
	private String userName;

	@NotBlank(message = "回复内容不能为空")
	@ApiModelProperty(name = "commentInfo",value = "评论内容",required = true,dataType = "String")
	private String commentInfo;
	
	@NotNull(message = "回复评论ID不能为空")
	@ApiModelProperty(name = "parentId",value = "父评论ID（0表示一级评论）",required = true,dataType = "Integer")
	private Integer parentId;
	
	@NotNull(message = "回复目标用户ID不能为空")
	@ApiModelProperty(name = "replyToUserId",value = "回复目标用户ID",required = true,dataType = "Integer")
	private Integer replyToUserId;
	
	@NotBlank(message = "回复目标用户名不能为空")
	@ApiModelProperty(name = "replyToUserName",value = "回复目标用户昵称",required = true,dataType = "String")
	private String replyToUserName;
	
}
