package com.playlet.oversea.query.drama;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 发表短剧评论（可评分）
 */
@Data
public class AddDramaCommentQuery {

	@NotNull(message = "剧ID不能为空")
	@ApiModelProperty(name = "dramaId", value = "剧ID", required = true, dataType = "Integer")
	private Integer dramaId;

	@NotNull(message = "评分不能为空")
	@Min(value = 1, message = "评分须为1-5")
	@Max(value = 5, message = "评分须为1-5")
	@ApiModelProperty(name = "score", value = "评分1-5", required = true, dataType = "Integer")
	private Integer score;

	@NotNull(message = "评论用户ID不能为空")
	@ApiModelProperty(name = "userId", value = "评论用户ID", required = true, dataType = "Integer")
	private Integer userId;

	@ApiModelProperty(name = "userName", value = "评论用户名（已废弃，服务端以账号昵称为准，可不传）", required = false, dataType = "String")
	private String userName;

	@NotBlank(message = "评论内容不能为空")
	@ApiModelProperty(name = "commentInfo", value = "评论内容", required = true, dataType = "String")
	private String commentInfo;
}
