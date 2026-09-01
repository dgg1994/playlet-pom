package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 新增/编辑持卡人入参（对齐 worldpay AppUserHolderEntity）。
 */
@Data
@ApiModel(value = "持卡人保存", description = "新增或编辑持卡人；编辑时传 id")
public class WalletCardholderSaveRequest {

	@ApiModelProperty(value = "持卡人 id，编辑时必传")
	private Long id;

	@ApiModelProperty(value = "英文名", required = true)
	private String userName;

	@ApiModelProperty(value = "英文姓", required = true)
	private String userSurname;

	@ApiModelProperty(value = "手机号区号（纯数字，如 852、86，不要带 +）", required = true)
	private String userTelDialCode;

	@ApiModelProperty(value = "国家/地区 ISO Alpha-3（如 HKG、CHN、SGP）")
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

	@ApiModelProperty(value = "出生日期 yyyy-MM-dd", required = true)
	private String userBirthday;
}
