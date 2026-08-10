package com.playlet.oversea.entity.drama;

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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_interact_message")
@ApiModel(value = "互动消息", description = "用户互动消息")
public class UserInteractMessageEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键ID", dataType = "Long")
	private Long id;

	@TableField("to_uid")
	@ApiModelProperty(name = "toUid", value = "接收人uid", dataType = "Integer")
	private Integer toUid;

	@TableField("from_uid")
	@ApiModelProperty(name = "fromUid", value = "触发人uid", dataType = "Integer")
	private Integer fromUid;

	@TableField("message_type")
	@ApiModelProperty(name = "messageType", value = "消息类型", dataType = "String")
	private String messageType;

	@TableField("biz_id")
	@ApiModelProperty(name = "bizId", value = "业务唯一ID", dataType = "String")
	private String bizId;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "短剧ID", dataType = "Integer")
	private Integer dramaId;

	@TableField("episode_id")
	@ApiModelProperty(name = "episodeId", value = "剧集ID", dataType = "String")
	private String episodeId;

	@TableField("comment_id")
	@ApiModelProperty(name = "commentId", value = "评论ID", dataType = "Integer")
	private Integer commentId;

	@TableField("reply_comment_id")
	@ApiModelProperty(name = "replyCommentId", value = "回复评论ID", dataType = "Integer")
	private Integer replyCommentId;

	@TableField("content")
	@ApiModelProperty(name = "content", value = "内容快照", dataType = "String")
	private String content;

	@TableField("is_read")
	@ApiModelProperty(name = "isRead", value = "是否已读 0未读1已读", dataType = "Integer")
	private Integer isRead;

	@TableField("read_time")
	@ApiModelProperty(name = "readTime", value = "已读时间", dataType = "Date")
	private Date readTime;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "状态 1有效0删除", dataType = "Integer")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
