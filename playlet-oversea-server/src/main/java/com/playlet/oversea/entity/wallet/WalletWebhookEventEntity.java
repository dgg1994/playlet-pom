package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 钱包 WebHook 事件（幂等）。
 */
@Data
@TableName("wallet_webhook_event")
@ApiModel(value = "钱包Webhook事件", description = "回调幂等与审计")
public class WalletWebhookEventEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("event_id")
	@ApiModelProperty(name = "eventId", value = "事件唯一键")
	private String eventId;

	@TableField("event_type")
	@ApiModelProperty(name = "eventType", value = "事件类型")
	private String eventType;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包 uid")
	private Long walletUid;

	@TableField("user_bankcard_id")
	@ApiModelProperty(name = "userBankcardId", value = "卡 id")
	private Long userBankcardId;

	@TableField("biz_no")
	@ApiModelProperty(name = "bizNo", value = "业务单号")
	private String bizNo;

	@TableField("payload")
	@ApiModelProperty(name = "payload", value = "回调原文")
	private String payload;

	@TableField("process_status")
	@ApiModelProperty(name = "processStatus", value = "0待处理 1成功 2失败 3忽略")
	private Integer processStatus;

	@TableField("process_msg")
	@ApiModelProperty(name = "processMsg", value = "处理说明")
	private String processMsg;

	@TableField("retry_count")
	@ApiModelProperty(name = "retryCount", value = "重试次数")
	private Integer retryCount;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "接收时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
