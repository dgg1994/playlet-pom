package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端充值订单列表/详情项。
 */
@Data
@ApiModel(value = "充值订单项", description = "统一订单展示")
public class RechargeOrderItemEntity {

	@ApiModelProperty("订单号")
	private String orderNo;

	@ApiModelProperty("用户ID")
	private Integer uid;

	@ApiModelProperty("用户昵称")
	private String nickname;

	@ApiModelProperty("商品类型 MEMBER / EPISODE")
	private String productType;

	@ApiModelProperty("商品展示名")
	private String productName;

	@ApiModelProperty("实付金额(元)；金币支付时可能为 0")
	private BigDecimal payAmount;

	@ApiModelProperty("金币实付")
	private Integer payCoin;

	@ApiModelProperty("支付方式 WECHAT/ALIPAY/BANK/COIN")
	private String payChannel;

	@ApiModelProperty("支付状态 0待支付 1已支付 2已关闭 3已退款")
	private Integer payStatus;

	@ApiModelProperty("优惠减免")
	private BigDecimal discountAmt;

	@ApiModelProperty("持券ID")
	private Integer userCouponId;

	@ApiModelProperty("三方单号")
	private String thirdTradeNo;

	@ApiModelProperty("支付时间")
	private Date payTime;

	@ApiModelProperty("下单时间")
	private Date setTime;

	@ApiModelProperty("会员开通天数（仅 MEMBER）")
	private Integer durationDays;

	@ApiModelProperty("剧ID（仅 EPISODE）")
	private Integer dramaId;

	@ApiModelProperty("集ID（仅 EPISODE）")
	private Integer assetId;
}
