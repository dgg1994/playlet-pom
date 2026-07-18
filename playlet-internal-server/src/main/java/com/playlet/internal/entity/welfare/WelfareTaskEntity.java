package com.playlet.internal.entity.welfare;

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
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("welfare_task")
@ApiModel(value = "福利任务配置", description = "福利任务逻辑配置（文案见 welfare_task_i18n）")
public class WelfareTaskEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("task_code")
	@ApiModelProperty(name = "taskCode", value = "任务编码，新增后不可改，如 WATCH_EP", dataType = "String")
	private String taskCode;

	@TableField("reward_coin")
	@ApiModelProperty(name = "rewardCoin", value = "基础奖励金币", dataType = "Integer")
	private Integer rewardCoin;

	@TableField("ad_boost_coin")
	@ApiModelProperty(name = "adBoostCoin", value = "看广告额外金币", dataType = "Integer")
	private Integer adBoostCoin;

	@TableField("cycle_type")
	@ApiModelProperty(name = "cycleType", value = "周期：1每日 2一次性 3每周 4每月，见 WelfareCycleTypeEnums", dataType = "Integer")
	private Integer cycleType;

	@TableField("target_count")
	@ApiModelProperty(name = "targetCount", value = "达标次数", dataType = "Integer")
	private Integer targetCount;

	@TableField("auto_claim")
	@ApiModelProperty(name = "autoClaim", value = "0手动领取 1自动发奖", dataType = "Integer")
	private Integer autoClaim;

	@TableField("expire_days")
	@ApiModelProperty(name = "expireDays", value = "有效天数，0永久", dataType = "Integer")
	private Integer expireDays;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "排序权重", dataType = "Integer")
	private Integer sortWeight;

	@TableField("task_icon")
	@ApiModelProperty(name = "taskIcon", value = "默认图标URL", dataType = "String")
	private String taskIcon;

	@TableField("extra_config")
	@ApiModelProperty(name = "extraConfig", value = "JSON扩展配置", dataType = "String")
	private String extraConfig;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用", dataType = "Integer")
	private Integer status;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "运营备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty(name = "taskName", value = "当前语言任务名（列表展示）", dataType = "String")
	private String taskName;

	@TableField(exist = false)
	@ApiModelProperty(name = "i18nList", value = "多语言文案", dataType = "List")
	private List<WelfareTaskI18nEntity> i18nList;
}
