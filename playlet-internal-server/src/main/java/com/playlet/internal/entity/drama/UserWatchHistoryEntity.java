package com.playlet.internal.entity.drama;

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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_watch_history")
@ApiModel(value = "用户观看历史", description = "用户短剧浏览/观看历史")
public class UserWatchHistoryEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键ID", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "String")
	private Integer uid;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "短剧业务ID", dataType = "String")
	private String dramaId;

	@TableField("episode_id")
	@ApiModelProperty(name = "episodeId", value = "最近观看的分集业务ID，未开播可为空", dataType = "String")
	private String episodeId;

	@TableField("episode_no")
	@ApiModelProperty(name = "episodeNo", value = "最近观看的集序号", dataType = "Integer")
	private Integer episodeNo;

	@TableField("watch_progress")
	@ApiModelProperty(name = "watchProgress", value = "分集内播放进度，单位：秒", dataType = "Integer")
	private Integer watchProgress;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "首次写入浏览历史的时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "最近一次观看/上报进度的时间", dataType = "Date")
	private Date gmtModified;
}
