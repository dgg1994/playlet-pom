package com.playlet.oversea.entity.wallet;

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
 * 钱包内部转账费率配置。
 */
@Data
@TableName("wallet_transfet_rates")
@ApiModel(value = "钱包内部转账费率", description = "内部转账手续费率")
public class WalletTransfetRatesEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("rates")
	@ApiModelProperty(name = "rates", value = "费率")
	private BigDecimal rates;

	@TableField("set_user")
	@ApiModelProperty(name = "setUser", value = "操作人")
	private Integer setUser;

	@TableField("set_user_name")
	@ApiModelProperty(name = "setUserName", value = "操作人姓名")
	private String setUserName;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
