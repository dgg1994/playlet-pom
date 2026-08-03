package com.playlet.internal.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_message_publish")
@ApiModel("系统消息发布主表")
public class SystemMessagePublishEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("message_type")
	@ApiModelProperty("NOTICE/ACTIVITY/VERSION/DRAMA_ONLINE/WITHDRAW/MEDAL/ACCOUNT")
	private String messageType;

	@TableField("default_langue")
	@ApiModelProperty("缺语言回退")
	private String defaultLangue;

	@TableField("drama_id")
	private Integer dramaId;

	@TableField("jump_type")
	private String jumpType;

	@TableField("jump_param")
	private String jumpParam;

	@TableField("priority")
	private Integer priority;

	@TableField("valid_start")
	private Date validStart;

	@TableField("valid_end")
	private Date validEnd;

	@TableField("audience_type")
	@ApiModelProperty("1全员 2指定uid")
	private Integer audienceType;

	@TableField("audience_json")
	@ApiModelProperty("指定uid JSON [1,2,3]")
	private String audienceJson;

	@TableField("push_flag")
	private Integer pushFlag;

	@TableField("publish_status")
	@ApiModelProperty("0草稿 1已发布 2取消")
	private Integer publishStatus;

	@TableField("schedule_time")
	private Date scheduleTime;

	@TableField("operator")
	private String operator;

	@TableField("status")
	@ApiModelProperty("1有效 0下架")
	private Integer status;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty("多语言列表（管理端）")
	private List<SystemMessagePublishI18nEntity> i18nList = new ArrayList<>();
}
