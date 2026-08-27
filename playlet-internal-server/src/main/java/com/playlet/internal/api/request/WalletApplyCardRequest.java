package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请开卡入参。
 */
@Data
@ApiModel(value = "申请开卡", description = "选择卡产品后申请")
public class WalletApplyCardRequest {

	@ApiModelProperty(value = "卡产品id（来自 /card/product/list 的 productId）", required = true)
	private Integer productId;

	@ApiModelProperty(value = "邮寄地址id，实体卡必传")
	private Integer deliveryAddressId;
}
