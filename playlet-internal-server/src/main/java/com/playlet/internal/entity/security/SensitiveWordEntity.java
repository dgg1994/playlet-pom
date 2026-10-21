package com.playlet.internal.entity.security;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 敏感词库（含分类与审核状态）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sensitive_word")
@ApiModel(value = "敏感词实体", description = "评论内容敏感词库")
public class SensitiveWordEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键ID", dataType = "Long")
	private Long id;

	@TableField("word_code")
	@ApiModelProperty(name = "wordCode", value = "业务编号，如 MG2026001", dataType = "String")
	private String wordCode;

	@TableField("word")
	@ApiModelProperty(name = "word", value = "敏感词内容", dataType = "String")
	private String word;

	@TableField("category")
	@ApiModelProperty(name = "category", value = "分类 FORBIDDEN/AD", dataType = "String")
	private String category;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "0待审核 1已生效 2已驳回 3已禁用", dataType = "Integer")
	private Integer status;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "添加人ID", dataType = "Integer")
	private Integer creatorId;

	@TableField("creator_name")
	@ApiModelProperty(name = "creatorName", value = "添加人展示名", dataType = "String")
	private String creatorName;

	@TableField("auditor_id")
	@ApiModelProperty(name = "auditorId", value = "审核人ID", dataType = "Integer")
	private Integer auditorId;

	@TableField("audit_time")
	@ApiModelProperty(name = "auditTime", value = "审核时间", dataType = "Date")
	private Date auditTime;

	@TableField("audit_remark")
	@ApiModelProperty(name = "auditRemark", value = "驳回/禁用备注", dataType = "String")
	private String auditRemark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
