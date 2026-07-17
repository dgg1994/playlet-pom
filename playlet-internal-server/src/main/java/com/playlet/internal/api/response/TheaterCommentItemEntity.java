package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("剧场一级评论条目")
public class TheaterCommentItemEntity {

	@ApiModelProperty("评论ID")
	private Long id;

	@ApiModelProperty("用户uid")
	private String uid;

	@ApiModelProperty("用户昵称")
	private String userName;

	@ApiModelProperty("用户头像")
	private String avatar;

	@ApiModelProperty("内容")
	private String content;

	@ApiModelProperty("点赞数")
	private Integer likeCount;

	@ApiModelProperty("当前用户是否已赞")
	private Boolean liked;

	@ApiModelProperty("回复总数")
	private Integer replyCount;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("相对时间文案")
	private String timeAgo;

	@ApiModelProperty("预览回复列表")
	private List<TheaterCommentReplyItemEntity> replies;
}
