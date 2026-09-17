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

/**
 * 观影礼奖励阶梯配置。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("watch_gift_reward_config")
@ApiModel(value = "观影礼奖励阶梯", description = "观影达标档位奖励配置")
public class WatchGiftRewardConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty("主键")
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
	@ApiModelProperty("备注")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty("创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty("更新时间")
	private Date gmtModified;
}
