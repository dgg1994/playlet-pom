package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 会员下单出参。
 */
@Data
@ApiModel("会员下单结果")
public class MemberOrderCreateRespEntity {

	@ApiModelProperty("业务订单号")
	private String orderNo;

	@ApiModelProperty("支付类型 COIN/CASH")
	private String payType;

	@ApiModelProperty("现金渠道；金币支付为空")
	private String payChannel;

	@ApiModelProperty("现金应付(元)")
	private BigDecimal payAmount;

	@ApiModelProperty("金币应付数量")
	private Integer payCoin;

	@ApiModelProperty("使用的持券ID")
	private Integer userCouponId;

	@ApiModelProperty("优惠减免")
	private BigDecimal discountAmt;

	@ApiModelProperty("支付状态：0待支付 1已支付（金币下单成功即为1）")
	private Integer payStatus;

	@ApiModelProperty("开通天数")
	private Integer durationDays;

	@ApiModelProperty("开通前到期时间快照")
	private Date expireBefore;

	@ApiModelProperty("预计/实际开通后到期时间")
	private Date expireAfter;
}
