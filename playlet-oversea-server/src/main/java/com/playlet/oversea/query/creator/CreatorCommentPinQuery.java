package com.playlet.oversea.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 作家端评论置顶请求。
 */
@Data
@ApiModel(value = "作家评论置顶", description = "置顶/取消置顶")
public class CreatorCommentPinQuery {

	@NotNull(message = "评论ID不能为空")
	@ApiModelProperty(value = "评论ID", required = true)
	private Integer commentId;

	@NotNull(message = "置顶标记不能为空")
	@ApiModelProperty(value = "1置顶 0取消", required = true)
	private Integer pinFlag;
}
