package com.playlet.internal.entity.medal;

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
@TableName("medal_config")
@ApiModel(value = "勋章配置", description = "勋章定义（文案见 medal_config_i18n）")
public class MedalConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("medal_code")
	@ApiModelProperty(name = "medalCode", value = "业务码，新增后不可改", dataType = "String")
	private String medalCode;

	@TableField("action_type")
	@ApiModelProperty(name = "actionType", value = "推进行为类型（复用 WelfareActionTypeEnums）", dataType = "String", example = "DRAMA_REVIEW")
	private String actionType;

	@TableField("target_count")
	@ApiModelProperty(name = "targetCount", value = "达标目标次数", dataType = "Integer")
	private Integer targetCount;

	@TableField("reward_coin")
	@ApiModelProperty(name = "rewardCoin", value = "解锁奖励金币", dataType = "Integer")
	private Integer rewardCoin;

	@TableField("icon_key")
	@ApiModelProperty(name = "iconKey", value = "已解锁图标七牛key", dataType = "String")
	private String iconKey;

	@TableField("icon_locked_key")
	@ApiModelProperty(name = "iconLockedKey", value = "未解锁图标七牛key", dataType = "String")
	private String iconLockedKey;

	@TableField("share_bg_key")
	@ApiModelProperty(name = "shareBgKey", value = "炫耀分享底图七牛key", dataType = "String")
	private String shareBgKey;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "排序权重", dataType = "Integer")
	private Integer sortWeight;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用0停用", dataType = "Integer")
	private Integer status;

	@TableField("is_deleted")
	@ApiModelProperty(name = "isDeleted", value = "软删除：0否1是", dataType = "Integer")
	private Integer isDeleted;

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
	@ApiModelProperty(name = "langue", value = "列表/筛选语言码", dataType = "String")
	private String langue;

	@TableField(exist = false)
	@ApiModelProperty(name = "logo", value = "图片", dataType = "String")
	private String logo;

	@TableField(exist = false)
	@ApiModelProperty(name = "medalName", value = "当前语言勋章名（列表展示）", dataType = "String")
	private String medalName;

	@TableField(exist = false)
	@ApiModelProperty(name = "slogan", value = "副文案/Slogan", dataType = "String")
	private String slogan;

	@TableField(exist = false)
	@ApiModelProperty(name = "conditionText", value = "条件展示文案", dataType = "String")
	private String conditionText;

	@TableField(exist = false)
	@ApiModelProperty(name = "unlockTime", value = "解锁时间", dataType = "Date")
	private Date unlockTime;

	@TableField(exist = false)
	@ApiModelProperty(name = "i18nList", value = "多语言文案", dataType = "List")
	private List<MedalConfigI18nEntity> i18nList;
}
