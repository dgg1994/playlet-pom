package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 卡产品标签（对齐 worldpay CardLableEntity）。
 */
@Data
@ApiModel(value = "卡产品标签", description = "申请页卡标签项")
public class WalletCardProductLabelResp {

	@ApiModelProperty("标签 id（本地无独立标签表时可为 null）")
	private Integer id;

	@ApiModelProperty("标签名称")
	private String name;
}
