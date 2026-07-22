package com.playlet.internal.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("watch_gift_global_config")
@ApiModel("观影礼全局配置")
public class WatchGiftGlobalConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("timezone")
	@ApiModelProperty("自然日时区")
	private String timezone;

	@TableField("min_report_interval_sec")
	@ApiModelProperty("两次计入最小间隔秒")
	private Integer minReportIntervalSec;

	@TableField("max_delta_sec_per_report")
	@ApiModelProperty("单次上报最大计入秒")
	private Integer maxDeltaSecPerReport;

	@TableField("max_daily_seconds")
	@ApiModelProperty("当日累计封顶秒")
	private Integer maxDailySeconds;

	@TableField("status")
	@ApiModelProperty("1启用 0停用")
	private Integer status;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
