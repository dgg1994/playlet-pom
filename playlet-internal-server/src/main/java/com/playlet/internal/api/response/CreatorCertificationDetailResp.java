package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 认证管理：认证详情（草稿回显 + 审核状态）。
 */
@Data
@ApiModel(value = "创作者认证详情")
public class CreatorCertificationDetailResp {

	private Integer category;
	private Integer submitStatus;
	private Integer auditStatus;
	private String auditStatusText;
	private String rejectReason;
	private Date submitTime;
	private Date auditTime;

	@ApiModelProperty(value = "已选认证身份编码列表")
	private List<Integer> identityTypes;

	// 个人实名
	private String realName;
	private String idCardMask;
	private String idCardFront;
	private String idCardFrontUrl;
	private String idCardBack;
	private String idCardBackUrl;

	// 机构
	private String orgName;
	private String orgLicense;
	private String orgLicenseUrl;
	private String teamSize;

	// 联系信息 / 机构信息共用
	private String city;
	private String phone;
	private String wechat;
	private String email;

	// 服务内容
	private String genreType;
	private String market;
	private String field;
	private List<String> services;
	private String price;
	private String extra;

	private List<WorkItem> works;

	@Data
	@ApiModel(value = "认证作品")
	public static class WorkItem {
		private String name;
		private String key;
		@ApiModelProperty(value = "签名后图片URL")
		private String url;
	}
}
