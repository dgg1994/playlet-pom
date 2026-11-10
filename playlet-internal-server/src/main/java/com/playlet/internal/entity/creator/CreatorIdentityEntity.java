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
 * 创作者身份资质（编剧 / 演员 / 导演·制作 / 编剧机构）。
 */
@Data
@TableName("creator_identity")
@ApiModel(value = "创作者身份资质")
public class CreatorIdentityEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "creator_account.id")
	private Integer creatorId;

	@TableField("identity_type")
	@ApiModelProperty(name = "identityType", value = "1编剧 2演员 3导演/制作 4编剧机构")
	private Integer identityType;

	@TableField("real_name")
	@ApiModelProperty(name = "realName", value = "真实姓名")
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
	@ApiModelProperty(name = "idCardFront", value = "证件正面图key")
	private String idCardFront;

	@TableField("id_card_back")
	@ApiModelProperty(name = "idCardBack", value = "证件背面图key")
	private String idCardBack;

	@TableField("org_name")
	@ApiModelProperty(name = "orgName", value = "机构名称(identity_type=4)")
	private String orgName;

	@TableField("org_license")
	@ApiModelProperty(name = "orgLicense", value = "营业执照图key(identity_type=4)")
	private String orgLicense;

	@TableField("materials")
	@ApiModelProperty(name = "materials", value = "资质材料JSON")
	private String materials;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "补充说明")
	private String remark;

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "0未认证 1审核中 2已认证 3驳回")
	private Integer auditStatus;

	@TableField("reject_reason")
	@ApiModelProperty(name = "rejectReason", value = "驳回原因")
	private String rejectReason;

	@TableField("auditor_id")
	@ApiModelProperty(name = "auditorId", value = "审核员 sys_user.id")
	private Integer auditorId;

	@TableField("audit_time")
	@ApiModelProperty(name = "auditTime", value = "审核时间")
	private Date auditTime;

	@TableField("pass_time")
	@ApiModelProperty(name = "passTime", value = "认证通过时间")
	private Date passTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
