package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 钱包资金明细 / 提现记录列表项。
 */
@Data
@ApiModel(value = "钱包资金明细")
public class CreatorWalletFlowResp {

	@ApiModelProperty(value = "主键（流水 id 或提现申请 id）")
	private Long id;

	@ApiModelProperty(value = "发生时间 yyyy-MM-dd HH:mm")
	private String time;

	@ApiModelProperty(value = "标题")
	private String title;

	@ApiModelProperty(value = "描述")
	private String desc;

	@ApiModelProperty(value = "金额（绝对值，金币）")
	private Long amount;

	@ApiModelProperty(value = "方向：+ 收入 / - 支出")
	private String sign;

	@ApiModelProperty(value = "分类：收入/支出/分账/提现")
	private String category;

	@ApiModelProperty(value = "状态 code（提现记录使用）")
	private Integer statusCode;

	@ApiModelProperty(value = "状态文案：已到账/已完成/处理中/已失败")
	private String statusText;
}
