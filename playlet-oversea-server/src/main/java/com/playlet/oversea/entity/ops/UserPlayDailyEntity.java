package com.playlet.oversea.entity.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * 用户日播放时长。
 */
@Data
@TableName("user_play_daily")
@ApiModel(value = "用户日播放", description = "biz_date+uid 唯一，累计 play_seconds")
public class UserPlayDailyEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("biz_date")
	private String bizDate;

	@TableField("uid")
	private Integer uid;

	@TableField("play_seconds")
	private Integer playSeconds;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
