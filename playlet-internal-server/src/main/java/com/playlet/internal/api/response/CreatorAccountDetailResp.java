package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账号信息聚合接口返回。
 */
@Data
@ApiModel(value = "账号信息聚合")
public class CreatorAccountDetailResp {

	@ApiModelProperty(name = "overview", value = "账号概览")
	private Overview overview;

	@ApiModelProperty(name = "baseInfo", value = "基础资料")
	private BaseInfo baseInfo;

	@ApiModelProperty(name = "contacts", value = "联系方式")
	private List<ContactItem> contacts;

	@ApiModelProperty(name = "identities", value = "身份资质")
	private List<IdentityItem> identities;

	@ApiModelProperty(name = "security", value = "账号安全")
	private Security security;

	// -------- 嵌套类 --------

	@Data
	@ApiModel(value = "账号概览")
	public static class Overview {
		private Integer creatorId;
		private String nickname;
		private String avatarUrl;
		private List<Badge> badges;
		private BigDecimal score;
		private String joinedAt;
		private String intro;
		private Integer workCount;
		private Integer followerCount;
	}

	@Data
	@ApiModel(value = "徽章")
	public static class Badge {
		private String key;
		private String label;
	}

	@Data
	@ApiModel(value = "基础资料")
	public static class BaseInfo {
		private String nickname;
		private Integer identityType;
		private String identityTypeText;
		private List<IdentityOption> identityTypeOptions;
		private String city;
		private String expertise;
		private String intro;
	}

	@Data
	@ApiModel(value = "身份类型选项")
	public static class IdentityOption {
		private String label;
		private Integer value;
	}

	@Data
	@ApiModel(value = "联系方式项")
	public static class ContactItem {
		private String key;
		private String label;
		private Boolean bound;
		private String value;
		private String action;
	}

	@Data
	@ApiModel(value = "身份资质项")
	public static class IdentityItem {
		private Integer identityType;
		private String name;
		private Integer status;
		private String statusText;
		private String icon;
		private Boolean enabled;
		private String passTime;
	}

	@Data
	@ApiModel(value = "账号安全")
	public static class Security {
		private Boolean hasPassword;
		private Boolean cancelApplying;
		private CancelApplyStatus cancelApplyStatus;
	}

	@Data
	@ApiModel(value = "注销申请状态")
	public static class CancelApplyStatus {
		private Integer status;
		private String applyTime;
		private String cancelDeadline;
		private Boolean canRevoke;
		private List<String> blockReasons;
	}
}
