package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家入驻资料（KYC / 结算）。
 */
@Data
@TableName("creator_profile")
@ApiModel(value = "作家入驻资料", description = "与 creator_account 1:1")
public class CreatorProfileEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("identity_type")
	@ApiModelProperty(name = "identityType", value = "1个人创作者 2公司机构 3工作室团队")
	private Integer identityType;

	@TableField("creator_role")
	@ApiModelProperty(name = "creatorRole", value = "入驻身份 1出品方 2MCN机构 3独立创作者")
	private Integer creatorRole;

	@TableField("city")
	@ApiModelProperty(name = "city", value = "所在城市")
	private String city;

	@TableField("expertise")
	@ApiModelProperty(name = "expertise", value = "擅长领域，顿号分隔")
	private String expertise;

	@TableField("intro")
	@ApiModelProperty(name = "intro", value = "个人/团队简介")
	private String intro;

	@TableField("real_name")
	@ApiModelProperty(name = "realName", value = "真实姓名/法人姓名")
	private String realName;

	@TableField("id_card_no_cipher")
	@ApiModelProperty(name = "idCardNoCipher", value = "证件号密文")
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	@com.alibaba.fastjson.annotation.JSONField(serialize = false)
	private String idCardNoCipher;

	@TableField("id_card_hash")
	@ApiModelProperty(name = "idCardHash", value = "证件号哈希")
	@com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
	@com.alibaba.fastjson.annotation.JSONField(serialize = false)
	private String idCardHash;

	@TableField("id_card_front")
	@ApiModelProperty(name = "idCardFront", value = "证件正面图")
	private String idCardFront;

	@TableField("id_card_back")
	@ApiModelProperty(name = "idCardBack", value = "证件背面图")
	private String idCardBack;

	@TableField("bill_address")
	@ApiModelProperty(name = "billAddress", value = "账单寄送地址")
	private String billAddress;

	@TableField("qualify_desc")
	@ApiModelProperty(name = "qualifyDesc", value = "资历自述")
	private String qualifyDesc;

	@TableField("org_name")
	@ApiModelProperty(name = "orgName", value = "机构名称")
	private String orgName;

	@TableField("org_license")
	@ApiModelProperty(name = "orgLicense", value = "营业执照图")
	private String orgLicense;

	@TableField("identity_codes")
	@ApiModelProperty(name = "identityCodes", value = "注册时选择的身份 JSON 数组，如 [\"writer\",\"actor\"]")
	private String identityCodes;

	@TableField("genre_type")
	@ApiModelProperty(name = "genreType", value = "题材类型 实拍/动画/二次元/虚拟拍摄")
	private String genreType;

	@TableField("market")
	@ApiModelProperty(name = "market", value = "发行市场 国内/海外/国内+海外")
	private String market;

	@TableField("price_range")
	@ApiModelProperty(name = "priceRange", value = "期望合作单价")
	private String priceRange;

	@TableField("services")
	@ApiModelProperty(name = "services", value = "承接服务 JSON 数组")
	private String services;

	@TableField("taboo")
	@ApiModelProperty(name = "taboo", value = "创作雷点/其他补充说明")
	private String taboo;

	@TableField("wechat")
	@ApiModelProperty(name = "wechat", value = "联系微信")
	private String wechat;

	@TableField("works")
	@ApiModelProperty(name = "works", value = "以往作品 JSON [{name,link,cover}]")
	private String works;

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "入驻审核 0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@TableField("audit_reject_reason")
	@ApiModelProperty(name = "auditRejectReason", value = "入驻驳回原因")
	private String auditRejectReason;

	@TableField("audit_pass_time")
	@ApiModelProperty(name = "auditPassTime", value = "入驻通过时间")
	private Date auditPassTime;

	@TableField("auditor_id")
	@ApiModelProperty(name = "auditorId", value = "审核员 sys_user.id")
	private Integer auditorId;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
