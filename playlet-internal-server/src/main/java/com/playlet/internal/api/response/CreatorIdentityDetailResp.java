package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 身份资质详情（含驳回原因）。
 */
@Data
@ApiModel(value = "身份资质详情")
public class CreatorIdentityDetailResp {

	private Integer identityType;
	private Integer status;
	private String statusText;
	private String rejectReason;
	private String realName;
	private String idCardMask;
	private String idCardFrontUrl;
	private String idCardBackUrl;
	private String orgName;
	private String orgLicenseUrl;
	private String materials;
	private Date applyTime;
	private Date auditTime;
	private Date passTime;
}
