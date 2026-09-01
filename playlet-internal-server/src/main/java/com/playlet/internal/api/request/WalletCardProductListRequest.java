package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 卡产品列表查询（对齐 onetoken CardEntity findList 入参）。
 */
@Data
@ApiModel(value = "卡产品列表查询", description = "申请卡片选品；可按卡性质筛选")
public class WalletCardProductListRequest {

	@ApiModelProperty(value = "卡片性质：VIRTUAL 虚拟卡 / PHYSICAL 实体卡；不传返回全部")
	private String bankCardNature;
}
