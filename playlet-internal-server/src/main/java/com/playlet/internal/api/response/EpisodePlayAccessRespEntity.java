package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * C 端单集播放策略出参。
 */
@Data
@ApiModel("单集播放策略")
public class EpisodePlayAccessRespEntity {

	@ApiModelProperty("集ID")
	private Integer assetId;

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("集序号")
	private Integer setNum;

	@ApiModelProperty("是否可播放（可请求 getVideoUrl）")
	private Boolean canPlay;

	@ApiModelProperty("是否已解锁记录")
	private Boolean unlocked;

	@ApiModelProperty("当前用户是否有效会员")
	private Boolean member;

	@ApiModelProperty("是否会员短剧")
	private Boolean memberDrama;

	@ApiModelProperty("集解锁类型：0免费 1广告 2付费")
	private Integer unlockType;

	@ApiModelProperty("金币价（付费集）")
	private Integer priceCoin;

	@ApiModelProperty("现金价元（付费集）")
	private BigDecimal priceYuan;

	@ApiModelProperty("拦截原因 NEED_LOGIN/NEED_MEMBER/NEED_AD/NEED_PAY/NOT_ON_SHELF；可播为 null")
	private String blockReason;

	@ApiModelProperty("已解锁方式 FREE/AD/COIN/CASH/MEMBER")
	private String unlockWay;
}
