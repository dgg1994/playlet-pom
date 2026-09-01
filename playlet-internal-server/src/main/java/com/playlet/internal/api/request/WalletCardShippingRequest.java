package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 实体卡发货入参（对齐 worldpay CardShippingEntity）。
 */
@Data
@ApiModel(value = "实体卡发货", description = "POST /cardApply/shipping")
public class WalletCardShippingRequest {

	@ApiModelProperty(value = "申请单 id", required = true)
	private Long applyId;

	@ApiModelProperty(value = "物流单号", required = true)
	private String logisticsNum;

	@ApiModelProperty(value = "邮费")
	private BigDecimal logisticsMonery;

	@ApiModelProperty(value = "物流商")
	private String logisticsProviders;

	@ApiModelProperty(value = "操作人 id")
	private Integer operateUserId;

	@ApiModelProperty(value = "操作人名称")
	private String operateUserName;
}
