package com.playlet.internal.entity.pay;

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
 * 支付全局配置（结算周期与提现门槛）。
 */
@Data
@TableName("pay_global_config")
@ApiModel(value = "支付全局配置", description = "默认结算周期与提现最低金额")
public class PayGlobalConfigEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("settle_cycle")
	@ApiModelProperty(name = "settleCycle", value = "默认结算周期 T+1/T+3/T+7/MONTH", dataType = "String")
	private String settleCycle;

	@TableField("min_withdraw_amount")
	@ApiModelProperty(name = "minWithdrawAmount", value = "提现最低金额(元)", dataType = "BigDecimal")
	private BigDecimal minWithdrawAmount;

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
