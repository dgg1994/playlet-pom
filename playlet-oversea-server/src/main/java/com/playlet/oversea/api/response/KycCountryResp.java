package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay KYC 国家列表项。
 */
@Data
@ApiModel(value = "KYC国家", description = "国家列表 data 元素")
public class KycCountryResp {

	@ApiModelProperty("三位数字代码")
	private String code;

	@ApiModelProperty("是否需要补充证件")
	private Boolean needCertificate;

	@ApiModelProperty("国家名字")
	private String name;

	@ApiModelProperty("国家英文名")
	private String enName;

	@ApiModelProperty("二位字母代码")
	private String profValue;

	@ApiModelProperty("电话区号")
	private String telephoneCode;

	@ApiModelProperty("三位字母代码")
	private String value;
}
