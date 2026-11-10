package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 管理端：身份资质审核列表项。
 */
@Data
@ApiModel(value = "身份资质审核列表项")
public class CreatorIdentityManageItemResp {

	private Integer id;
	private Integer creatorId;
	private String creatorNickname;
	private String creatorAvatarUrl;
	private String creatorOrgName;
	private Integer identityType;
	private String identityTypeName;
	private String realName;
	private String orgName;
	private String orgLicenseUrl;
	private Integer status;
	private String statusText;
	private String rejectReason;
	private Date applyTime;
	private Date auditTime;
}
