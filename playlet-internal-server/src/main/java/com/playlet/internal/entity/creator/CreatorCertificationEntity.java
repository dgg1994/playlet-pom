package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 创作者认证申请（认证管理页：个人/机构双类别，草稿 + 提交两态）。
 */
@Data
@TableName("creator_certification")
@ApiModel(value = "创作者认证申请")
public class CreatorCertificationEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("category")
	@ApiModelProperty(name = "category", value = "类别 1个人创作者 2机构创作者")
	private Integer category;

	@TableField("identity_types")
	@ApiModelProperty(name = "identityTypes", value = "已选认证身份JSON数组，类别内编码 1编剧 2演员 3导演 4版权人")
	private String identityTypes;

	@TableField("real_name")
	@ApiModelProperty(name = "realName", value = "真实姓名(个人)")
	private String realName;

	@TableField("id_card_no_cipher")
	@ApiModelProperty(hidden = true)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String idCardNoCipher;

	@TableField("id_card_hash")
	@ApiModelProperty(hidden = true)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String idCardHash;

	@TableField("id_card_front")
	@ApiModelProperty(name = "idCardFront", value = "身份证正面图key")
	private String idCardFront;

	@TableField("id_card_back")
	@ApiModelProperty(name = "idCardBack", value = "身份证反面图key")
	private String idCardBack;

	@TableField("org_name")
	@ApiModelProperty(name = "orgName", value = "公司名称(机构)")
	private String orgName;

	@TableField("org_license")
	@ApiModelProperty(name = "orgLicense", value = "营业执照图key(机构)")
	private String orgLicense;

	@TableField("team_size")
	@ApiModelProperty(name = "teamSize", value = "团队人员数量(机构)")
	private String teamSize;

	@TableField("city")
	@ApiModelProperty(name = "city", value = "所在城市")
	private String city;

	@TableField("phone")
	@ApiModelProperty(name = "phone", value = "联系手机号")
	private String phone;

	@TableField("wechat")
	@ApiModelProperty(name = "wechat", value = "微信号")
	private String wechat;

	@TableField("email")
	@ApiModelProperty(name = "email", value = "邮箱")
	private String email;

	@TableField("genre_type")
	@ApiModelProperty(name = "genreType", value = "擅长类型")
	private String genreType;

	@TableField("market")
	@ApiModelProperty(name = "market", value = "擅长市场")
	private String market;

	@TableField("field")
	@ApiModelProperty(name = "field", value = "擅长领域(个人)")
	private String field;

	@TableField("services")
	@ApiModelProperty(name = "services", value = "承接服务JSON数组")
	private String services;

	@TableField("price")
	@ApiModelProperty(name = "price", value = "服务价格")
	private String price;

	@TableField("extra")
	@ApiModelProperty(name = "extra", value = "其他补充说明")
	private String extra;

	@TableField("works")
	@ApiModelProperty(name = "works", value = "作品JSON数组 [{\"name\":\"\",\"key\":\"\"}]")
	private String works;

	@TableField("submit_status")
	@ApiModelProperty(name = "submitStatus", value = "0草稿 1已提交")
	private Integer submitStatus;

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "0未提交 1审核中 2已通过 3已驳回")
	private Integer auditStatus;

	@TableField("reject_reason")
	@ApiModelProperty(name = "rejectReason", value = "驳回原因")
	private String rejectReason;

	@TableField("submit_time")
	@ApiModelProperty(name = "submitTime", value = "最近提交时间")
	private Date submitTime;

	@TableField("audit_time")
	@ApiModelProperty(name = "auditTime", value = "审核时间")
	private Date auditTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
