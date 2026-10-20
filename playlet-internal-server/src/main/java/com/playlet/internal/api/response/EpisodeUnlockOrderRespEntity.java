package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 剧集解锁下单/结果出参。
 */
@Data
@ApiModel("剧集解锁结果")
public class EpisodeUnlockOrderRespEntity {

	@ApiModelProperty("集ID")
	private Integer assetId;

	@ApiModelProperty("剧ID")
	private Integer dramaId;

	@ApiModelProperty("解锁方式 FREE/AD/COIN/CASH/MEMBER")
	private String unlockWay;

	@ApiModelProperty("业务订单号（现金/金币）或广告 transId")
	private String bizNo;

	@ApiModelProperty("支付状态：金币/广告解锁成功为 1；现金下单为 0 待支付")
	private Integer payStatus;

	@ApiModelProperty("现金应付(元)")
	private BigDecimal payAmount;

	@ApiModelProperty("金币实付")
	private Integer payCoin;

	@ApiModelProperty("现金渠道")
	private String payChannel;
}
