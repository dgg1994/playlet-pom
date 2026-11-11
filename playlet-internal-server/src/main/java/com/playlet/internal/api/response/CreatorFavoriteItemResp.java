package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 我的收藏 - 创作者收藏行。
 */
@Data
@ApiModel(value = "创作者收藏行")
public class CreatorFavoriteItemResp {

	@ApiModelProperty("创作者ID")
	private Integer creatorId;

	@ApiModelProperty("创作者名称")
	private String name;

	@ApiModelProperty("头像签名URL")
	private String avatarUrl;

	@ApiModelProperty("身份文案，如：编剧/演员")
	private String identityText;

	@ApiModelProperty("擅长领域")
	private String genresText;

	@ApiModelProperty("代表作品，多个用 / 拼接")
	private String worksText;

	@ApiModelProperty("接单状态")
	private String acceptStatus;

	@ApiModelProperty("收藏时间")
	private Date favoritedAt;

	@ApiModelProperty("收藏时间文案 yyyy-MM-dd")
	private String favoritedAtText;

	/** 以下仅用于内部转换，不输出 */
	@ApiModelProperty(hidden = true)
	private String identityTypes;

	@ApiModelProperty(hidden = true)
	private Integer identityCategory;

	@ApiModelProperty(hidden = true)
	private String field;

	@ApiModelProperty(hidden = true)
	private String works;
}
