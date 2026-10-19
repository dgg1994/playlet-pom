package com.playlet.internal.entity.coin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 久未登录清币流水。
 */
@Data
@TableName("user_coin_clear_log")
@ApiModel(value = "清币流水", description = "久未登录清空金币审计")
public class UserCoinClearLogEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("clear_coin")
	@ApiModelProperty(name = "clearCoin", value = "清掉的金币数", dataType = "Long")
	private Long clearCoin;

	@TableField("idle_days")
	@ApiModelProperty(name = "idleDays", value = "判定未登录天数", dataType = "Integer")
	private Integer idleDays;

	@TableField("last_login")
	@ApiModelProperty(name = "lastLogin", value = "清币时最后登录", dataType = "Date")
	private Date lastLogin;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "清币时间", dataType = "Date")
	private Date setTime;
}
