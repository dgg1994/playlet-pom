package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("互动消息列表条目")
public class TheaterInteractMessageItemEntity {

	@ApiModelProperty("消息ID")
	private Long id;

	@ApiModelProperty("消息类型")
	private String messageType;

	@ApiModelProperty("接收人uid")
	private Integer toUid;

	@ApiModelProperty("触发人uid")
	private Integer fromUid;

	@ApiModelProperty("触发人昵称")
	private String fromNickname;

	@ApiModelProperty("触发人头像")
	private String fromAvatar;

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("短剧标题")
	private String dramaTitle;

	@ApiModelProperty("短剧封面")
	private String dramaCoverUrl;

	@ApiModelProperty("剧集ID")
	private String episodeId;

	@ApiModelProperty("评论ID")
	private Integer commentId;

	@ApiModelProperty("回复评论ID")
	private Integer replyCommentId;

	@ApiModelProperty("内容快照")
	private String content;

	@ApiModelProperty("是否已读 0未读1已读")
	private Integer isRead;

	@ApiModelProperty("时间")
	private Date setTime;
}
