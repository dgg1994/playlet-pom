package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 作家结算账户（OnePay）展示。
 */
@Data
@ApiModel(value = "作家结算账户", description = "收益页当前绑定的 OnePay 账户摘要")
public class CreatorSettlementAccountRespEntity {

	@ApiModelProperty("OnePay 绑定 0未绑定 1已绑定")
	private Integer bindStatus;

	@ApiModelProperty("OnePay 账号")
	private String onepayAccountMasked;

	@ApiModelProperty("绑定成功时间 yyyy-MM-dd HH:mm:ss")
	private String bindTime;
}
