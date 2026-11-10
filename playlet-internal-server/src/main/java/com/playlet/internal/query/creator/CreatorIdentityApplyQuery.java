package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 身份认证提交。
 */
@Data
@ApiModel(value = "身份认证提交")
public class CreatorIdentityApplyQuery {

	@NotNull
	@ApiModelProperty(name = "identityType", value = "1编剧 2演员 3导演/制作 4编剧机构", required = true)
	private Integer identityType;

	@NotBlank
	@ApiModelProperty(name = "realName", value = "真实姓名", required = true)
	private String realName;

	@ApiModelProperty(name = "idCardNo", value = "证件号（明文，入库前加密）")
	private String idCardNo;

	@ApiModelProperty(name = "idCardFront", value = "证件正面图key")
	private String idCardFront;

	@ApiModelProperty(name = "idCardBack", value = "证件背面图key")
	private String idCardBack;

	@ApiModelProperty(name = "orgName", value = "机构名称(identityType=4 必填)")
	private String orgName;

	@ApiModelProperty(name = "orgLicense", value = "营业执照图key(identityType=4 必填)")
	private String orgLicense;

	@ApiModelProperty(name = "materials", value = "资质材料JSON数组 [{\"name\":\"\",\"key\":\"\"}]")
	private String materials;

	@ApiModelProperty(name = "remark", value = "补充说明")
	private String remark;
}
