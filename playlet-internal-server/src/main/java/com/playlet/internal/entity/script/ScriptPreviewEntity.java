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
 * 剧本市场-剧本试读。
 */
@Data
@TableName("script_preview")
@ApiModel(value = "剧本试读", description = "场景与台词")
public class ScriptPreviewEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("episode_no")
	@ApiModelProperty(name = "episodeNo", value = "所属集号")
	private Integer episodeNo;

	@TableField("scene_no")
	@ApiModelProperty(name = "sceneNo", value = "场景序号")
	private Integer sceneNo;

	@TableField("scene_title")
	@ApiModelProperty(name = "sceneTitle", value = "场景标题")
	private String sceneTitle;

	@TableField("content_json")
	@ApiModelProperty(name = "contentJson", value = "台词行 JSON")
	private String contentJson;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
