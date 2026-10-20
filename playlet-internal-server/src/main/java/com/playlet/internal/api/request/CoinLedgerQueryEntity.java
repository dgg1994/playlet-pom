package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端金币流水分页查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("金币流水查询")
public class CoinLedgerQueryEntity extends PageQueryHelperEntity {

	@ApiModelProperty("业务类型，可选，如 MEMBER_BUY / EPISODE_UNLOCK")
	private String bizType;
}
