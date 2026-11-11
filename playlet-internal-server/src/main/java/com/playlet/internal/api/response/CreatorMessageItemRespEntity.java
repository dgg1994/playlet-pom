package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 作家端消息中心列表项。
 */
@Data
@ApiModel("作家站内信列表项")
public class CreatorMessageItemRespEntity {

	@ApiModelProperty("来源 INBOX=收件箱 BROADCAST=站务广播")
	private String source;

	@ApiModelProperty("广播用 publishId；收件箱用 inboxId")
	private Long id;

	@ApiModelProperty("收件箱消息ID，广播时为空")
	private Long inboxId;

	@ApiModelProperty("发布单ID，评审直发可空")
	private Long publishId;

	@ApiModelProperty("AUDIT评审 SITE系统公告 COOP合作 SUBMIT征稿投稿 CONTRACT合同 INCOME收益结算")
	private String messageType;

	@ApiModelProperty("分类展示名，如 系统公告消息")
	private String categoryName;

	@ApiModelProperty("标题")
	private String title;

	@ApiModelProperty("正文")
	private String content;

	@ApiModelProperty("封面图 URL（已签名）")
	private String coverUrl;

	@ApiModelProperty("关联短剧ID")
	private Integer dramaId;

	@ApiModelProperty("关联剧集ID，剧级可空")
	private Integer assetId;

	@ApiModelProperty("跳转类型 none/drama/asset")
	private String jumpType;

	@ApiModelProperty("跳转参数 JSON")
	private String jumpParam;

	@ApiModelProperty("优先级，广播可置顶")
	private Integer priority;

	@ApiModelProperty("是否已读 0未读 1已读")
	private Integer isRead;

	@ApiModelProperty("创建时间")
	private Date setTime;

	@ApiModelProperty("创建时间文案 yyyy-MM-dd HH:mm")
	private String timeText;
}
