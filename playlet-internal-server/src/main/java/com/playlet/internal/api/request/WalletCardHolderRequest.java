package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 开卡持卡人信息（对齐 worldpay AppUserHolderEntity）。
 */
@Data
@ApiModel(value = "开卡持卡人", description = "申请开卡时的持卡人资料")
public class WalletCardHolderRequest {

	@ApiModelProperty(value = "英文名", required = true)
	private String userName;

	@ApiModelProperty(value = "英文姓", required = true)
	private String userSurname;

	@ApiModelProperty(value = "手机号区号", required = true)
	private String userTelDialCode;

	@ApiModelProperty(value = "地区编码")
	private String userTelCode;

	@ApiModelProperty(value = "手机号", required = true)
	private String userTel;

	@ApiModelProperty(value = "邮箱", required = true)
	private String userEmail;

	@ApiModelProperty(value = "证件号")
	private String userNumber;

	@ApiModelProperty(value = "性别文案")
	private String userSex;

	@ApiModelProperty(value = "性别编号：1男 2女")
	private Integer userSexNum;

	@ApiModelProperty(value = "住址")
	private String userAddress;

	@ApiModelProperty(value = "出生日期 yyyy-MM-dd")
	private String userBirthday;
}
