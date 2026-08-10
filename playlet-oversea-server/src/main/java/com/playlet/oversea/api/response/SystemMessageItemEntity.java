package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("系统消息列表项")
public class SystemMessageItemEntity {

	@ApiModelProperty("来源 BROADCAST=广播 INBOX=收件箱")
	private String source;

	@ApiModelProperty("广播用 publishId；收件箱用 inboxId")
	private Long id;

	@ApiModelProperty("收件箱消息ID，广播时为空")
	private Long inboxId;

	@ApiModelProperty("发布单ID，业务直发可空")
	private Long publishId;

	@ApiModelProperty("消息类型 NOTICE/ACTIVITY/VERSION/DRAMA_ONLINE/WITHDRAW/MEDAL/ACCOUNT")
	private String messageType;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("正文")
	private String content;

	@ApiModelProperty("封面图 URL（已签名）")
	private String coverUrl;

	@ApiModelProperty("关联短剧ID，无则空")
	private Integer dramaId;

	@ApiModelProperty("跳转类型 none/drama/url/page/withdraw/medal")
	private String jumpType;

	@ApiModelProperty("跳转参数 JSON，如 {\"dramaId\":100}")
	private String jumpParam;

	@ApiModelProperty("优先级 0普通 1置顶")
	private Integer priority;

	@ApiModelProperty("是否已读 0未读 1已读")
	private Integer isRead;

	@ApiModelProperty("创建时间")
	private Date setTime;
}
