package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 开卡申请 KYC 证件快照（对齐 worldpay CardApplyKycEntity）。
 */
@Data
@ApiModel(value = "开卡KYC快照", description = "申请开卡时附带的证件信息；不传则从账户 KYC 回填")
public class WalletCardApplyKycRequest {

	@ApiModelProperty(value = "证件类型 PASSPORT / NATIONAL_ID")
	private String paperworkType;

	@ApiModelProperty(value = "证件号码")
	private String paperworkNum;

	@ApiModelProperty(value = "证件到期时间")
	private String expirationTime;

	@ApiModelProperty(value = "正面照 id")
	private String frontPhotoId;

	@ApiModelProperty(value = "正面照 url")
	private String frontPhotoUrl;

	@ApiModelProperty(value = "反面照 id")
	private String backPhotoId;

	@ApiModelProperty(value = "反面照 url")
	private String backPhotoUrl;

	@ApiModelProperty(value = "手持证件照 id")
	private String handheldPhotoId;

	@ApiModelProperty(value = "手持证件照 url")
	private String handheldPhotoUrl;
}
