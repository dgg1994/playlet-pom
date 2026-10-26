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
 * 金币日汇率。
 */
@Data
@TableName("coin_rate_daily")
@ApiModel(value = "金币日汇率", description = "按日广告公式或录入的汇率")
public class CoinRateDailyEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("biz_date")
	@ApiModelProperty(name = "bizDate", value = "业务日", dataType = "Date")
	private Date bizDate;

	@TableField("ad_total")
	@ApiModelProperty(name = "adTotal", value = "当日激励广告完成次数", dataType = "Long")
	private Long adTotal;

	@TableField("ecpm")
	@ApiModelProperty(name = "ecpm", value = "元/千次", dataType = "BigDecimal")
	private BigDecimal ecpm;

	@TableField("r_rate")
	@ApiModelProperty(name = "rRate", value = "当日分成系数", dataType = "BigDecimal")
	private BigDecimal rRate;

	@TableField("coin_total")
	@ApiModelProperty(name = "coinTotal", value = "当日发放金币总量", dataType = "Long")
	private Long coinTotal;

	@TableField("p_coin")
	@ApiModelProperty(name = "pCoin", value = "1币兑人民币(元)", dataType = "BigDecimal")
	private BigDecimal pCoin;

	@TableField("coin_per_yuan")
	@ApiModelProperty(name = "coinPerYuan", value = "多少币≈1元", dataType = "Integer")
	private BigDecimal coinPerYuan;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
