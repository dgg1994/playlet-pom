package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@ApiModel("短剧评论定位请求")
public class DramaCommentLocateQuery {

	@NotNull(message = "剧ID不能为空")
	@ApiModelProperty(name = "dramaId", value = "剧ID", required = true, dataType = "Integer")
	private Integer dramaId;

	@NotNull(message = "评论id不能为空")
	@ApiModelProperty(name = "commentId", value = "要定位的评论id", required = true, dataType = "Integer")
	private Integer commentId;

	@Min(value = 1, message = "pageSize须大于0")
	@ApiModelProperty(name = "pageSize", value = "回复列表页大小，须与 /dramaComment/reply/list 一致，默认10", dataType = "Integer")
	private Integer pageSize;

	@Min(value = 1, message = "parentPageSize须大于0")
	@ApiModelProperty(name = "parentPageSize", value = "一级列表页大小，须与 /dramaComment/list 一致，默认10", dataType = "Integer")
	private Integer parentPageSize;
}
