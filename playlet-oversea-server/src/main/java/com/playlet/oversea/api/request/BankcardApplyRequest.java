package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请银行卡入参。
 */
@Data
@ApiModel(value = "申请银行卡", description = "POST /api/bankcard/apply")
public class BankcardApplyRequest {

	@ApiModelProperty(value = "产品id（商户卡产品列表返回 id）", required = true)
	private Integer productId;

	@ApiModelProperty(value = "邮寄地址id，实体卡必传")
	private Integer deliveryAddressId;
}
