package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("金币流水项")
public class CoinLedgerItemEntity {

	@ApiModelProperty("流水ID")
	private Long id;

	@ApiModelProperty("变动金额")
	private Integer changeAmt;

	@ApiModelProperty("变动后余额")
	private Long balanceAfter;

	@ApiModelProperty("业务类型")
	private String bizType;

	@ApiModelProperty("业务类型文案")
	private String bizTypeLabel;

	@ApiModelProperty("幂等业务键")
	private String bizId;

	@ApiModelProperty("关联任务编码")
	private String taskCode;

	@ApiModelProperty("是否广告加赠")
	private Integer adBoostFlag;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("时间")
	private Date setTime;
}
