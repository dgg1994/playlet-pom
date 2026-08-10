package com.playlet.oversea.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("system_message_publish_i18n")
@ApiModel("系统消息多语言")
public class SystemMessagePublishI18nEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("publish_id")
	private Long publishId;

	@TableField("langue")
	@ApiModelProperty("语言码 zh-cn/en")
	private String langue;

	@TableField("title")
	private String title;

	@TableField("content")
	private String content;

	@TableField("cover_url")
	private String coverUrl;

	@TableField("jump_param")
	private String jumpParam;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
