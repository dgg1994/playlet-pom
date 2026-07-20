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
@TableName("user_welfare_watch_log")
@ApiModel(value = "看剧任务去重日志", description = "当日已计集去重")
public class UserWelfareWatchLogEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户uid", dataType = "String")
	private Integer uid;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "短剧ID", dataType = "Integer")
	private Integer dramaId;

	@TableField("episode_id")
	@ApiModelProperty(name = "episodeId", value = "分集ID（drama_asset.id）", dataType = "String")
	private String episodeId;

	@TableField("biz_date")
	@ApiModelProperty(name = "bizDate", value = "业务日期 yyyy-MM-dd", dataType = "String")
	private String bizDate;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;
}
