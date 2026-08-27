package com.playlet.internal.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * worldPay 提交 KYC 信息请求体。
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "提交KYC信息", description = "POST /api/user/kyc/apply body")
public class KycApplyRequest {

	@ApiModelProperty(value = "英文名", required = true)
	private String firstName;

	@ApiModelProperty(value = "英文姓", required = true)
	private String lastName;

	@ApiModelProperty(value = "证件号", required = true)
	private String idNo;

	@ApiModelProperty(value = "电子邮件", required = true)
	private String email;

	@ApiModelProperty(value = "国籍 ISO Alpha-3，如 SGP", required = true)
	private String nationCode;

	@ApiModelProperty(value = "证件类型：1身份证 2护照 3驾照", required = true)
	private Integer certType;

	@ApiModelProperty(value = "证件照正面 url", required = true)
	private String idUrl;

	@ApiModelProperty(value = "证件照反面 url")
	private String idBackUrl;

	@ApiModelProperty(value = "生日", required = true)
	private String birthday;

	@ApiModelProperty(value = "居住国家 ISO Alpha-3", required = true)
	private String countryCode;

	@ApiModelProperty(value = "手机区号", required = true)
	private String areaCode;

	@ApiModelProperty(value = "手机号", required = true)
	private String phone;

	@ApiModelProperty(value = "补充资料文件类型（needCertificate=true 时传）")
	private Integer fileType;

	@ApiModelProperty(value = "补充资料文件 url")
	private String fileUrl;

	@ApiModelProperty(value = "人脸报告 url")
	private String faceUrl;

	@ApiModelProperty(value = "人脸报告第三方 sessionId")
	private String referenceId;

	@ApiModelProperty(value = "第三方报告类型，如 SUMSUB")
	private String referenceType;

	@ApiModelProperty(value = "人脸自拍照 url")
	private String selfieUrl;
}
