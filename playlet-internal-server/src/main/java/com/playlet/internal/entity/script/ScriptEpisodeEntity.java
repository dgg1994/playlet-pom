package com.playlet.internal.entity.script;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 剧本市场-分集结构。
 */
@Data
@TableName("script_episode")
@ApiModel(value = "剧本分集", description = "分集结构与预估时长")
public class ScriptEpisodeEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("episode_no")
	@ApiModelProperty(name = "episodeNo", value = "集号")
	private Integer episodeNo;

	@TableField("episode_title")
	@ApiModelProperty(name = "episodeTitle", value = "分集标题")
	private String episodeTitle;

	@TableField("duration_seconds")
	@ApiModelProperty(name = "durationSeconds", value = "预估时长（秒）")
	private Integer durationSeconds;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
