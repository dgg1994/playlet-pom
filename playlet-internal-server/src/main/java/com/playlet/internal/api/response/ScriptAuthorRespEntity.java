package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 剧本编剧信息。
 */
@Data
@ApiModel(value = "剧本编剧", description = "市场卡片与详情共用")
public class ScriptAuthorRespEntity {

	@ApiModelProperty("编剧ID（creator_account.id）")
	private Integer id;

	@ApiModelProperty("编剧名称")
	private String name;

	@ApiModelProperty("头像签名URL")
	private String avatarUrl;

	@ApiModelProperty("头像底色（前端占位）")
	private String initialColor;

	@ApiModelProperty("剧本数")
	private Integer scriptCount;

	@ApiModelProperty("评分")
	private BigDecimal score;

	@ApiModelProperty("是否已关注")
	private Boolean followed;

	@JsonIgnore
	@ApiModelProperty("关联统一账号 sys_user.id，仅用于关注查询")
	private Integer sysUserId;
}
