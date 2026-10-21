package com.playlet.internal.entity.coupon;

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
 * 优惠券发放流水。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coupon_issue_log")
@ApiModel(value = "优惠券发放流水", description = "管理端发放批次记录")
public class CouponIssueLogEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("batch_no")
	@ApiModelProperty(name = "batchNo", value = "发放批次号", dataType = "String")
	private String batchNo;

	@TableField("template_id")
	@ApiModelProperty(name = "templateId", value = "模板ID", dataType = "Integer")
	private Integer templateId;

	@TableField("coupon_code")
	@ApiModelProperty(name = "couponCode", value = "券编码快照", dataType = "String")
	private String couponCode;

	@TableField("issue_type")
	@ApiModelProperty(name = "issueType", value = "MANUAL/EVENT", dataType = "String")
	private String issueType;

	@TableField("issue_count")
	@ApiModelProperty(name = "issueCount", value = "本批发出张数", dataType = "Integer")
	private Integer issueCount;

	@TableField("target_desc")
	@ApiModelProperty(name = "targetDesc", value = "发放对象摘要", dataType = "String")
	private String targetDesc;

	@TableField("operator_id")
	@ApiModelProperty(name = "operatorId", value = "操作人ID", dataType = "Integer")
	private Integer operatorId;

	@TableField("operator_name")
	@ApiModelProperty(name = "operatorName", value = "操作人展示名", dataType = "String")
	private String operatorName;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
