package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 作家端回复评论请求。
 */
@Data
@ApiModel(value = "作家回复评论", description = "以作者身份回复指定评论")
public class CreatorCommentReplyQuery {

	@NotNull(message = "评论ID不能为空")
	@ApiModelProperty(value = "被回复的评论ID", required = true)
	private Integer commentId;

	@NotBlank(message = "回复内容不能为空")
	@ApiModelProperty(value = "回复内容", required = true)
	private String commentInfo;
}
