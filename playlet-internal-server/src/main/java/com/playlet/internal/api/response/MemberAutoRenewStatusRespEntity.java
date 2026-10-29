package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 连续包月状态。
 */
@Data
@ApiModel("连续包月状态")
public class MemberAutoRenewStatusRespEntity {

	@ApiModelProperty("是否开启自动续费")
	private Boolean autoRenew;

	@ApiModelProperty("签约渠道")
	private String payChannel;

	@ApiModelProperty("签约状态：WAIT待签约 / SIGNED已签约 / UNSIGNED已解约 / INVALID失效；未签约时为 null")
	private String agreementStatus;

	@ApiModelProperty("三方协议号")
	private String agreementNo;

	@ApiModelProperty("续费套餐ID")
	private Integer productId;

	@ApiModelProperty("续费套餐名")
	private String productName;

	@ApiModelProperty("每期扣款金额(元)")
	private BigDecimal renewPrice;

	@ApiModelProperty("每期天数")
	private Integer periodDays;

	@ApiModelProperty("下次扣款时间")
	private Date nextDeductTime;
}
