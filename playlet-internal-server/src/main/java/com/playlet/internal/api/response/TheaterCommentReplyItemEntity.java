package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("剧场评论回复条目")
public class TheaterCommentReplyItemEntity {

	@ApiModelProperty("回复ID")
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

	@ApiModelProperty("被回复用户昵称")
	private String replyToUserName;

	@ApiModelProperty("被回复用户uid")
	private String replyToUid;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("相对时间文案")
	private String timeAgo;
}
