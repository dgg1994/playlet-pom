package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.config.jackson.JsonFlexibleStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 征稿投稿记录（参与征稿页面提交的稿件）。
 */
@Data
@TableName("creator_call_submission")
@ApiModel(value = "征稿投稿记录")
public class CreatorCallSubmissionEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("call_id")
	@ApiModelProperty(name = "callId", value = "creator_call.id")
	private Integer callId;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "投稿人 creator_account.id")
	private Integer creatorId;

	@TableField("creator_name")
	@ApiModelProperty(name = "creatorName", value = "投稿人昵称")
	private String creatorName;

	@ApiModelProperty(name = "title", value = "剧本名称")
	private String title;

	@ApiModelProperty(name = "intro", value = "剧本简介")
	private String intro;

	@TableField("attachments")
	@JsonDeserialize(using = JsonFlexibleStringDeserializer.class)
	@ApiModelProperty(name = "attachments", value = "稿件附件 JSON [{name,key}]")
	private String attachments;

	@ApiModelProperty(name = "status", value = "稿件状态 1已投稿 2已入围 3未入围")
	private Integer status;

	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState", value = "删除状态 0正常 1删除")
	private Integer deleteState;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "投稿时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
