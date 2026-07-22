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
@TableName("user_sign_in_log")
@ApiModel(value = "用户签到明细", description = "每用户每日最多一条")
public class UserSignInLogEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户uid", dataType = "String")
	private String uid;

	@TableField("biz_date")
	@ApiModelProperty(name = "bizDate", value = "签到归属日 yyyy-MM-dd", dataType = "String")
	private String bizDate;

	@TableField("sign_type")
	@ApiModelProperty(name = "signType", value = "1正常签到 2补签", dataType = "Integer")
	private Integer signType;

	@TableField("streak_days")
	@ApiModelProperty(name = "streakDays", value = "本次后的连续天数快照", dataType = "Integer")
	private Integer streakDays;

	@TableField("reward_day_index")
	@ApiModelProperty(name = "rewardDayIndex", value = "按第几天档位发奖", dataType = "Integer")
	private Integer rewardDayIndex;

	@TableField("reward_coin")
	@ApiModelProperty(name = "rewardCoin", value = "到账金币", dataType = "Integer")
	private Integer rewardCoin;

	@TableField("cost_coin")
	@ApiModelProperty(name = "costCoin", value = "补签扣金币（旧），正常签为0", dataType = "Integer")
	private Integer costCoin;

	@TableField("cost_card")
	@ApiModelProperty(name = "costCard", value = "补签消耗卡数，正常签为0", dataType = "Integer")
	private Integer costCard;

	@TableField("ad_flag")
	@ApiModelProperty(name = "adFlag", value = "1广告免扣（旧）", dataType = "Integer")
	private Integer adFlag;

	@TableField("ad_ticket")
	@ApiModelProperty(name = "adTicket", value = "广告凭证", dataType = "String")
	private String adTicket;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;
}
