package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 注册认证一次性提交（注册流程最后一步「提交审核」）。
 */
@Data
@ApiModel(value = "注册认证提交", description = "身份、实名、基础信息、承接服务、以往作品一次性提交，提交后进入审核")
public class CreatorRegisterSubmitQuery {

	@ApiModelProperty(name = "identityType", value = "1个人创作者 2创作机构")
	private Integer identityType;

	@ApiModelProperty(name = "identities", value = "选择的身份编码，如 [\"writer\",\"actor\",\"director\",\"copyright\"]")
	private List<String> identities;

	@ApiModelProperty(name = "nickname", value = "展示昵称")
	private String nickname;

	@ApiModelProperty(name = "avatar", value = "头像")
	private String avatar;

	@ApiModelProperty(name = "city", value = "所在城市")
	private String city;

	@ApiModelProperty(name = "wechat", value = "联系微信")
	private String wechat;

	@ApiModelProperty(name = "realName", value = "真实姓名")
	private String realName;

	@ApiModelProperty(name = "idNumber", value = "证件号（明文，入库前加密）")
	private String idNumber;

	@ApiModelProperty(name = "idCardFront", value = "证件正面图 key/url")
	private String idCardFront;

	@ApiModelProperty(name = "idCardBack", value = "证件背面图 key/url")
	private String idCardBack;

	@ApiModelProperty(name = "genreType", value = "题材类型")
	private String genreType;

	@ApiModelProperty(name = "market", value = "发行市场")
	private String market;

	@ApiModelProperty(name = "expertise", value = "擅长领域/擅长分项")
	private String expertise;

	@ApiModelProperty(name = "priceRange", value = "期望合作单价")
	private String priceRange;

	@ApiModelProperty(name = "services", value = "承接服务（可多选）")
	private List<String> services;

	@ApiModelProperty(name = "taboo", value = "创作雷点/其他补充说明")
	private String taboo;

	@ApiModelProperty(name = "intro", value = "个人/团队简介")
	private String intro;

	@ApiModelProperty(name = "orgName", value = "机构名称（机构认证必填）")
	private String orgName;

	@ApiModelProperty(name = "orgLicense", value = "营业执照图（机构认证必填）")
	private String orgLicense;

	@ApiModelProperty(name = "billAddress", value = "账单寄送地址")
	private String billAddress;

	@ApiModelProperty(name = "works", value = "以往作品")
	private List<WorkItem> works;

	@Data
	@ApiModel(value = "以往作品")
	public static class WorkItem {

		@ApiModelProperty(name = "name", value = "作品名称")
		private String name;

		@ApiModelProperty(name = "link", value = "作品链接")
		private String link;

		@ApiModelProperty(name = "cover", value = "作品封面图 key/url")
		private String cover;
	}
}
