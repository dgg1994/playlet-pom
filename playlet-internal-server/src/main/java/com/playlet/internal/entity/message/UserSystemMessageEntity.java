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

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_system_message")
@ApiModel("用户系统消息收件箱")
public class UserSystemMessageEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("to_uid")
	private Integer toUid;

	@TableField("publish_id")
	private Long publishId;

	@TableField("message_type")
	private String messageType;

	@TableField("langue")
	private String langue;

	@TableField("title")
	private String title;

	@TableField("content")
	private String content;

	@TableField("cover_url")
	private String coverUrl;

	@TableField("drama_id")
	private Integer dramaId;

	@TableField("biz_id")
	@ApiModelProperty("幂等键")
	private String bizId;

	@TableField("jump_type")
	private String jumpType;

	@TableField("jump_param")
	private String jumpParam;

	@TableField("is_read")
	private Integer isRead;

	@TableField("status")
	private Integer status;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
