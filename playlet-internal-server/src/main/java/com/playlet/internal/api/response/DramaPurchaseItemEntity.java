package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 短剧付费详情-购买记录项。
 */
@Data
@ApiModel(value = "剧集购买记录项", description = "短剧下单笔购买明细")
public class DramaPurchaseItemEntity {

	@ApiModelProperty("订单号")
	private String orderNo;

	@ApiModelProperty("用户ID")
	private Integer uid;

	@ApiModelProperty("用户昵称")
	private String nickname;

	@ApiModelProperty("购买内容文案")
	private String purchaseContent;

	@ApiModelProperty("集ID")
	private Integer assetId;

	@ApiModelProperty("集数")
	private Integer setNum;

	@ApiModelProperty("金额(元)")
	private BigDecimal payAmount;

	@ApiModelProperty("支付方式 WECHAT/ALIPAY/BANK/COIN")
	private String payChannel;

	@ApiModelProperty("购买时间")
	private Date payTime;

	@ApiModelProperty("状态 0待支付 1已支付 2已关闭 3已退款")
	private Integer payStatus;
}
