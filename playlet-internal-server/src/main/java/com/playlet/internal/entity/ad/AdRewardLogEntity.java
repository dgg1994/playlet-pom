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

import java.math.BigDecimal;
import java.util.Date;

/**
 * 激励广告校验流水。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ad_reward_log")
@ApiModel(value = "激励广告流水", description = "广告完成校验与防刷")
public class AdRewardLogEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("scene")
	@ApiModelProperty(name = "scene", value = "UNLOCK/TASK_BOOST 等", dataType = "String")
	private String scene;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "剧ID", dataType = "Integer")
	private Integer dramaId;

	@TableField("asset_id")
	@ApiModelProperty(name = "assetId", value = "集ID", dataType = "Integer")
	private Integer assetId;

	@TableField("slot_code")
	@ApiModelProperty(name = "slotCode", value = "广告位业务码", dataType = "String")
	private String slotCode;

	@TableField("trans_id")
	@ApiModelProperty(name = "transId", value = "广告平台交易号", dataType = "String")
	private String transId;

	@TableField("verify_ok")
	@ApiModelProperty(name = "verifyOk", value = "0未通过 1通过", dataType = "Integer")
	private Integer verifyOk;

	@TableField("ecpm")
	@ApiModelProperty(name = "ecpm", value = "本次eCPM(元/千次)", dataType = "BigDecimal")
	private BigDecimal ecpm;

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
