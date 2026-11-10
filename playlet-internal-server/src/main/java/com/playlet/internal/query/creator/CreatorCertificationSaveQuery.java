package com.playlet.internal.query.creator;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 认证管理：保存草稿 / 提交审核入参（个人 + 机构共用，按 category 区分）。
 */
@Data
@ApiModel(value = "创作者认证保存入参")
public class CreatorCertificationSaveQuery {

	@NotNull
	@ApiModelProperty(name = "category", value = "类别 1个人创作者 2机构创作者", required = true)
	private Integer category;

	@ApiModelProperty(name = "identityTypes", value = "已选认证身份，类别内编码 1编剧 2演员 3导演 4版权人")
	private List<Integer> identityTypes;

	@ApiModelProperty(name = "realName", value = "真实姓名(个人)")
	private String realName;

	@ApiModelProperty(name = "idCardNo", value = "证件号（明文，入库前加密；掩码回填不重复加密）")
	private String idCardNo;

	@ApiModelProperty(name = "idCardFront", value = "身份证正面图key")
	private String idCardFront;

	@ApiModelProperty(name = "idCardBack", value = "身份证反面图key")
	private String idCardBack;

	@ApiModelProperty(name = "orgName", value = "公司名称(机构)")
	private String orgName;

	@ApiModelProperty(name = "orgLicense", value = "营业执照图key(机构)")
	private String orgLicense;

	@ApiModelProperty(name = "teamSize", value = "团队人员数量(机构)")
	private String teamSize;

	@ApiModelProperty(name = "city", value = "所在城市")
	private String city;

	@ApiModelProperty(name = "phone", value = "联系手机号")
	private String phone;

	@ApiModelProperty(name = "wechat", value = "微信号")
	private String wechat;

	@ApiModelProperty(name = "email", value = "邮箱")
	private String email;

	@ApiModelProperty(name = "genreType", value = "擅长类型")
	private String genreType;

	@ApiModelProperty(name = "market", value = "擅长市场")
	private String market;

	@ApiModelProperty(name = "field", value = "擅长领域(个人)")
	private String field;

	@ApiModelProperty(name = "services", value = "承接服务")
	private List<String> services;

	@ApiModelProperty(name = "price", value = "服务价格")
	private String price;

	@ApiModelProperty(name = "extra", value = "其他补充说明")
	private String extra;

	@ApiModelProperty(name = "works", value = "作品列表")
	private List<WorkItem> works;

	@Data
	@ApiModel(value = "认证作品")
	public static class WorkItem {

		@ApiModelProperty(name = "name", value = "作品名")
		private String name;

		@ApiModelProperty(name = "key", value = "作品图key")
		private String key;
	}
}
