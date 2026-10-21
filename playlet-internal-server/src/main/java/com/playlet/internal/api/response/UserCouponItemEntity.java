package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * C 端我的优惠券列表项。
 */
@Data
@ApiModel(value = "我的优惠券", description = "用户持券展示")
public class UserCouponItemEntity {

	@ApiModelProperty("持券ID")
	private Integer id;

	@ApiModelProperty("模板ID")
	private Integer templateId;

	@ApiModelProperty("券编码")
	private String couponCode;

	@ApiModelProperty("券名称")
	private String couponName;

	@ApiModelProperty("类型")
	private String couponType;

	@ApiModelProperty("面额文案")
	private String faceValueText;

	@ApiModelProperty("币种")
	private String currency;

	@ApiModelProperty("适用范围")
	private String scope;

	@ApiModelProperty("折扣率")
	private BigDecimal discountRate;

	@ApiModelProperty("满减门槛")
	private BigDecimal thresholdAmt;

	@ApiModelProperty("减免额")
	private BigDecimal reduceAmt;

	@ApiModelProperty("0未用 1已用 2过期 3作废")
	private Integer status;

	@ApiModelProperty("过期时间")
	private Date expireTime;

	@ApiModelProperty("领取时间")
	private Date setTime;
}
