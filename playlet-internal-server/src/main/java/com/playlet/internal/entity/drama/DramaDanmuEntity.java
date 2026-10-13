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

/**
 * 短剧弹幕。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drama_danmu")
@ApiModel(value = "短剧弹幕", description = "按集+播放秒展示的弹幕")
public class DramaDanmuEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "短剧ID", dataType = "Integer")
	private Integer dramaId;

	@TableField("video_id")
	@ApiModelProperty(name = "videoId", value = "集ID，对应 drama_asset.id", dataType = "Integer")
	private Integer videoId;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "发送用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("content")
	@ApiModelProperty(name = "content", value = "弹幕正文", dataType = "String")
	private String content;

	@TableField("play_time")
	@ApiModelProperty(name = "playTime", value = "出现在视频中的秒数（从0起）", dataType = "Integer")
	private Integer playTime;

	@TableField("color")
	@ApiModelProperty(name = "color", value = "颜色，如 #FFFFFF", dataType = "String")
	private String color;

	@TableField("mode")
	@ApiModelProperty(name = "mode", value = "1滚动 2顶部 3底部", dataType = "Integer")
	private Integer mode;

	@TableField("font_size")
	@ApiModelProperty(name = "fontSize", value = "1标准 2加大", dataType = "Integer")
	private Integer fontSize;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1正常 0隐藏 2用户自删", dataType = "Integer")
	private Integer status;

	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState", value = "0正常 1软删", dataType = "Integer")
	private Integer deleteState;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "发送时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;

	/** 非表字段：发送者昵称（列表联查） */
	@TableField(exist = false)
	@ApiModelProperty(name = "nickname", value = "发送者昵称", dataType = "String")
	private String nickname;

	/** 非表字段：发送者头像（列表联查） */
	@TableField(exist = false)
	@ApiModelProperty(name = "avatar", value = "发送者头像", dataType = "String")
	private String avatar;
}
