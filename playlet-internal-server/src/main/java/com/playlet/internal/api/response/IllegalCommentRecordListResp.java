package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("违规评论列表项")
public class IllegalCommentRecordListResp {

	@ApiModelProperty("记录ID")
	private Long id;

	@ApiModelProperty("评论ID")
	private Integer commentId;

	@ApiModelProperty("用户ID")
	private Integer userId;

	@ApiModelProperty("用户头像")
	private String userAvatar;

	@ApiModelProperty("用户昵称")
	private String userNickname;

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("短剧名称")
	private String dramaTitle;

	@ApiModelProperty("剧集ID")
	private Integer episodeId;

	@ApiModelProperty("短剧集数")
	private Integer episodeNum;

	@ApiModelProperty("评论类型：PUBLISH=发表评论，REPLY=回复评论，BLOCKED=未入库拦截")
	private String commentActionType;

	@ApiModelProperty("评论内容")
	private String content;

	@ApiModelProperty("命中敏感词")
	private String sensitiveWords;

	@ApiModelProperty("风险等级")
	private Integer riskLevel;

	@ApiModelProperty("状态")
	private Integer status;

	@ApiModelProperty("处理类型")
	private Integer handleType;

	@ApiModelProperty("处理备注")
	private String handleRemark;

	@ApiModelProperty("来源类型")
	private Integer sourceType;

	@ApiModelProperty("创建时间")
	private Date setTime;
}
