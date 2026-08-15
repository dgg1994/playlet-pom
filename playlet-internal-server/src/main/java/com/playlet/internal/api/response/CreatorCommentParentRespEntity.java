package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作家评论管理：被引用的父评摘要。
 */
@Data
@ApiModel(value = "作家评论父评摘要", description = "回复卡片中的引用区")
public class CreatorCommentParentRespEntity {

	@ApiModelProperty("父评ID")
	private Integer id;

	@ApiModelProperty("用户昵称")
	private String userName;

	@ApiModelProperty("用户头像（已签名）")
	private String avatar;

	@ApiModelProperty("评论内容")
	private String commentInfo;
}
