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

	@ApiModelProperty("消息类型 LIKE_DRAMA/LIKE_COMMENT/COMMENT_DRAMA/COMMENT_VIDEO/REPLY_DRAMA/REPLY_VIDEO")
	private String messageType;

	@ApiModelProperty("展示文案，如：赞了你的评论、回复你")
	private String actionText;

	@ApiModelProperty("接收人uid")
	private Integer toUid;

	@ApiModelProperty("触发人uid")
	private Integer fromUid;

	@ApiModelProperty("触发人昵称")
	private String fromNickname;

	@ApiModelProperty("触发人头像")
	private String fromAvatar;

	@ApiModelProperty("短剧ID（跳转用）")
	private Integer dramaId;

	@ApiModelProperty("短剧标题文案，如：岁月苛相望")
	private String dramaTitle;

	@ApiModelProperty("来源按钮文案，如：出自《岁月苛相望》")
	private String sourceText;

	@ApiModelProperty("短剧封面")
	private String dramaCoverUrl;

	@ApiModelProperty("剧集ID（跳转用）")
	private String episodeId;

	@ApiModelProperty("评论ID（点赞/回复操作目标，跳转用）")
	private Integer commentId;

	@ApiModelProperty("父评论ID（跳转用）")
	private Integer replyCommentId;

	@ApiModelProperty("主内容文案（回复/评论正文），如：知识产权律师")
	private String content;

	@ApiModelProperty("列表主文案，如：回复你：知识产权律师")
	private String displayContent;

	@ApiModelProperty("引用评论文案（灰色竖线区域），如：富士山下")
	private String refContent;

	@ApiModelProperty("是否展示点赞/回复按钮 1是0否（回复/评论类为1）")
	private Integer showActions;

	@ApiModelProperty("当前用户是否已点赞该评论 1是0否")
	private Integer isLiked;

	@ApiModelProperty("是否已读 0未读1已读")
	private Integer isRead;

	@ApiModelProperty("时间")
	private Date setTime;
}
