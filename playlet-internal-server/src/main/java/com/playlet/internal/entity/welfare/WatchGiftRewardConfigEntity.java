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
@TableName("watch_gift_reward_config")
@ApiModel("观影礼奖励阶梯")
public class WatchGiftRewardConfigEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("gear_index")
	@ApiModelProperty("档位序号，claim 入参")
	private Integer gearIndex;

	@TableField("target_seconds")
	@ApiModelProperty("达标累计秒数")
	private Integer targetSeconds;

	@TableField("reward_coin")
	@ApiModelProperty("奖励金币")
	private Integer rewardCoin;

	@TableField("status")
	@ApiModelProperty("1启用 0停用")
	private Integer status;

	@TableField("remark")
	private String remark;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
