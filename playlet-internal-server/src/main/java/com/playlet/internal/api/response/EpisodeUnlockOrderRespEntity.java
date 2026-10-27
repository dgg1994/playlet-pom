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

	@ApiModelProperty("解锁结果：UNLOCKED=已解锁（金币/广告/会员直通）；WAIT_PAY=待支付（现金下单）")
	private String unlockStatus;

	@ApiModelProperty("解锁范围：SINGLE=仅当前集；REMAINING=当前集及之后全部付费集")
	private String unlockScope;

	@ApiModelProperty("本次解锁的集数（REMAINING 时为实际解锁集数）")
	private Integer unlockCount;

	@ApiModelProperty("现金应付(元)")
	private BigDecimal payAmount;

	@ApiModelProperty("金币实付")
	private Integer payCoin;

	@ApiModelProperty("使用的持券ID")
	private Integer userCouponId;

	@ApiModelProperty("优惠减免")
	private BigDecimal discountAmt;

	@ApiModelProperty("现金渠道")
	private String payChannel;

	@ApiModelProperty("三方下单结果（现金下单时返回，失败时 success=false 可用 /api/pay/prepay 重试）")
	private PayPrepayResult payInfo;
}
