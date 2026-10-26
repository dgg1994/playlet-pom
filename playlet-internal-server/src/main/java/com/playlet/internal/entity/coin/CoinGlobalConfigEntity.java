package com.playlet.internal.entity.coin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 金币全局配置。
 */
@Data
@TableName("coin_global_config")
@ApiModel(value = "金币全局配置", description = "汇率模式与清币策略")
public class CoinGlobalConfigEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("r_rate")
	@ApiModelProperty(name = "rRate", value = "广告收入分配系数", dataType = "BigDecimal")
	private BigDecimal rRate;

	@TableField("manual_coin_per_yuan")
	@ApiModelProperty(name = "manualCoinPerYuan", value = "手动汇率：多少币≈1元", dataType = "Integer")
	private BigDecimal manualCoinPerYuan;

	@TableField("rate_mode")
	@ApiModelProperty(name = "rateMode", value = "1手动 2按日广告公式", dataType = "Integer")
	private Integer rateMode;

	@TableField("idle_clear_days")
	@ApiModelProperty(name = "idleClearDays", value = "连续未登录清币天数", dataType = "Integer")
	private Integer idleClearDays;

	@TableField("idle_clear_enabled")
	@ApiModelProperty(name = "idleClearEnabled", value = "1开启清币 0关闭", dataType = "Integer")
	private Integer idleClearEnabled;

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
