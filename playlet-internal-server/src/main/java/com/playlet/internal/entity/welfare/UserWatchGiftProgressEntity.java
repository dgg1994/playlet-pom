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
@TableName("user_watch_gift_progress")
@ApiModel("用户观影礼每日进度")
public class UserWatchGiftProgressEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("uid")
	private String uid;

	@TableField("biz_date")
	@ApiModelProperty("业务日 yyyy-MM-dd")
	private String bizDate;

	@TableField("watch_seconds")
	@ApiModelProperty("当日已计入秒数")
	private Integer watchSeconds;

	@TableField("claimed_gears")
	@ApiModelProperty("已领档位，如 1,2")
	private String claimedGears;

	@TableField("last_report_time")
	@ApiModelProperty("上次计入时间")
	private Date lastReportTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
