package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("关注/粉丝列表条目")
public class UserFollowItemEntity {

	@ApiModelProperty("对方uid")
	private Integer uid;

	@ApiModelProperty("昵称")
	private String nickname;

	@ApiModelProperty("头像")
	private String avatar;

	@ApiModelProperty("关注时间")
	private Date setTime;

	@ApiModelProperty("当前登录用户是否已关注对方")
	private Boolean followed;
}
