package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 拒绝开卡申请入参（对齐 worldpay RejectApplyEntity）。
 */
@Data
@ApiModel(value = "拒绝开卡申请", description = "POST /cardApply/reject")
public class WalletCardApplyRejectRequest {

	@ApiModelProperty(value = "申请单 id", required = true)
	private Long id;

	@ApiModelProperty(value = "拒绝原因", required = true)
	private String rejectInfo;
}
