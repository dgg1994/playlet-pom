package com.playlet.oversea.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 作家端回复评论：commentType=1 剧集评 / 2 短剧评。
 */
@Data
@ApiModel(value = "作家回复评论", description = "按 commentType 区分短剧/剧集；不可评分")
public class CreatorCommentReplyQuery {

	@NotNull(message = "剧ID不能为空")
	@ApiModelProperty(name = "dramaId", value = "剧ID", required = true, dataType = "Integer")
	private Integer dramaId;

	@ApiModelProperty(name = "commentType", value = "1剧集评 2短剧评，默认1", required = false, dataType = "Integer")
	private Integer commentType;

	@ApiModelProperty(name = "videoId", value = "视频id（commentType=1 必填）", required = false, dataType = "Integer")
	private Integer videoId;

	@NotBlank(message = "回复内容不能为空")
	@ApiModelProperty(name = "commentInfo", value = "评论内容", required = true, dataType = "String")
	private String commentInfo;

	@NotNull(message = "回复评论ID不能为空")
	@ApiModelProperty(name = "parentId", value = "父评论ID", required = true, dataType = "Integer")
	private Integer parentId;

	@NotNull(message = "回复目标用户ID不能为空")
	@ApiModelProperty(name = "replyToUserId", value = "回复目标用户ID", required = true, dataType = "Integer")
	private Integer replyToUserId;

	@ApiModelProperty(name = "replyToUserName", value = "回复目标用户昵称（已废弃，可不传）", required = false, dataType = "String")
	private String replyToUserName;
}
