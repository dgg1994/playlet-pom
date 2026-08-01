package com.playlet.internal.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("评论定位请求")
public class CommentLocateQuery {

	@NotNull(message = "视频id不能为空")
	@ApiModelProperty(name = "videoId", value = "视频id", required = true, dataType = "Integer")
	private Integer videoId;

	@NotNull(message = "评论id不能为空")
	@ApiModelProperty(name = "commentId", value = "要定位的评论id", required = true, dataType = "Integer")
	private Integer commentId;

	@Min(value = 1, message = "pageSize须大于0")
	@ApiModelProperty(name = "pageSize", value = "回复列表页大小，须与 /reply/list 一致，默认10", dataType = "Integer")
	private Integer pageSize;

	@Min(value = 1, message = "parentPageSize须大于0")
	@ApiModelProperty(name = "parentPageSize", value = "一级列表页大小，须与 /list 一致，默认10", dataType = "Integer")
	private Integer parentPageSize;
}
