package com.playlet.internal.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sign_in_global_config")
@ApiModel(value = "签到全局配置", description = "签到规则（通常一行）")
public class SignInGlobalConfigEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("timezone")
	@ApiModelProperty(name = "timezone", value = "签到自然日时区", dataType = "String")
	private String timezone;

	@TableField("cycle_mode")
	@ApiModelProperty(name = "cycleMode", value = "CAP封顶 / LOOP循环", dataType = "String")
	private String cycleMode;

	@TableField("cycle_days")
	@ApiModelProperty(name = "cycleDays", value = "阶梯天数", dataType = "Integer")
	private Integer cycleDays;

	@TableField("makeup_enabled")
	@ApiModelProperty(name = "makeupEnabled", value = "1开放补签 0关闭", dataType = "Integer")
	private Integer makeupEnabled;

	@TableField("makeup_window_days")
	@ApiModelProperty(name = "makeupWindowDays", value = "可补近N天（不含今天）", dataType = "Integer")
	private Integer makeupWindowDays;

	@TableField("makeup_month_limit")
	@ApiModelProperty(name = "makeupMonthLimit", value = "每月补签次数上限", dataType = "Integer")
	private Integer makeupMonthLimit;

	@TableField("makeup_cost_coin")
	@ApiModelProperty(name = "makeupCostCoin", value = "单次补签扣金币，0不扣", dataType = "Integer")
	private Integer makeupCostCoin;

	@TableField("makeup_allow_ad")
	@ApiModelProperty(name = "makeupAllowAd", value = "1允许广告免扣 0不允许（已废弃，保留兼容）", dataType = "Integer")
	private Integer makeupAllowAd;

	@TableField("makeup_cost_card")
	@ApiModelProperty(name = "makeupCostCard", value = "每次补签消耗补签卡数", dataType = "Integer")
	private Integer makeupCostCard;

	@TableField("makeup_buy_month_limit")
	@ApiModelProperty(name = "makeupBuyMonthLimit", value = "每月限购补签卡张数", dataType = "Integer")
	private Integer makeupBuyMonthLimit;

	@TableField("makeup_buy_price_coin")
	@ApiModelProperty(name = "makeupBuyPriceCoin", value = "购买一张补签卡所需金币，0不开放购买", dataType = "Integer")
	private Integer makeupBuyPriceCoin;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用", dataType = "Integer")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
