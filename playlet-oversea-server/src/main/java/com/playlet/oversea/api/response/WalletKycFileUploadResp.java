package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * KYC 证件文件上传结果（对齐 worldPay POST /api/file/upload）。
 */
@Data
@ApiModel(value = "KYC文件上传结果", description = "返回三方 fileUrl，供 kyc/apply 使用")
public class WalletKycFileUploadResp {

	@ApiModelProperty(value = "文件 url 链接", required = true)
	private String fileUrl;
}
