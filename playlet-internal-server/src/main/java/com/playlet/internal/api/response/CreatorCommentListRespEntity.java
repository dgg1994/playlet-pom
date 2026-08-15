package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家评论管理列表行。
 */
@Data
@ApiModel(value = "作家评论列表行", description = "一级评论或回复卡片")
public class CreatorCommentListRespEntity {

	@ApiModelProperty("评论ID")
	private Integer id;

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("剧名")
	private String dramaTitle;

	@ApiModelProperty("集资源ID（drama_asset.id）")
	private Integer videoId;

	@ApiModelProperty("集序号")
	private Integer setNum;

	@ApiModelProperty("上下文文案，如：在 xxx 第05集 发表了评论")
	private String contextText;

	@ApiModelProperty("是否回复 1是0否")
	private Integer replyFlag;

	@ApiModelProperty("父评论ID，一级为0")
	private Integer parentId;

	@ApiModelProperty("评论用户ID（作家回复时为占位0）")
	private Integer userId;

	@ApiModelProperty("作家ID（作者身份回复时有值）")
	private Integer fromCreatorId;

	@ApiModelProperty("用户昵称")
	private String userName;

	@ApiModelProperty("用户头像（已签名）")
	private String avatar;

	@ApiModelProperty("评论内容")
	private String commentInfo;

	@ApiModelProperty("点赞数")
	private Integer likeCount;

	@ApiModelProperty("是否置顶 1是0否")
	private Integer pinFlag;

	@ApiModelProperty("置顶时间")
	private Date pinTime;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("若为回复，被引用的父评")
	private CreatorCommentParentRespEntity parentComment;
}
