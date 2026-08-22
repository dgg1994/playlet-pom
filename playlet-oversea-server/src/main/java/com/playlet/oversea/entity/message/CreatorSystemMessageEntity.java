package com.playlet.oversea.entity.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 作家端系统消息收件箱。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("creator_system_message")
@ApiModel("作家系统消息收件箱")
public class CreatorSystemMessageEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("to_creator_id")
	@ApiModelProperty("接收作家 creator_account.id")
	private Integer toCreatorId;

	@TableField("publish_id")
	@ApiModelProperty("关联站务发布单，业务直发可空")
	private Long publishId;

	@TableField("message_type")
	@ApiModelProperty("AUDIT评审 SITE站务")
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

	@TableField("asset_id")
	private Integer assetId;

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
