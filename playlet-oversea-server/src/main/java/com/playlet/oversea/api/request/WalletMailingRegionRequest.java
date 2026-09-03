package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 查询邮寄地区列表（对齐 worldPay POST /api/delivery/region）。
 */
@Data
@ApiModel(value = "邮寄地区列表", description = "POST /wallet/mailing/region")
public class WalletMailingRegionRequest {

	@ApiModelProperty(value = "地区语言，如 zh_CN / zh_HK / en_US；不传则按请求头 language 推断")
	private String local;
}
