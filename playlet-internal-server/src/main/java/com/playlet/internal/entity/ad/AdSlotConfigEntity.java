package com.playlet.internal.entity.ad;

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
 * 广告位配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_slot_config")
@ApiModel(value = "广告位配置", description = "业务码与广告平台广告位映射")
public class AdSlotConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("slot_code")
	@ApiModelProperty(name = "slotCode", value = "业务码", dataType = "String")
	private String slotCode;

	@TableField("slot_name")
	@ApiModelProperty(name = "slotName", value = "名称", dataType = "String")
	private String slotName;

	@TableField("platform")
	@ApiModelProperty(name = "platform", value = "CSJ/GDT 等", dataType = "String")
	private String platform;

	@TableField("platform_slot")
	@ApiModelProperty(name = "platformSlot", value = "广告平台广告位ID", dataType = "String")
	private String platformSlot;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用", dataType = "Integer")
	private Integer status;

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
