package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("用户关注统计")
public class UserFollowStatEntity {

	@ApiModelProperty("目标用户uid")
	private String uid;

	@ApiModelProperty("关注数")
	private Long followCount;

	@ApiModelProperty("粉丝数")
	private Long fansCount;

	@ApiModelProperty("当前登录用户是否已关注该用户")
	private Boolean followed;
}
