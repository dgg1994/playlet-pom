package com.playlet.oversea.api.request;

import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询邮寄地址（本地分页，对齐 onetoken POST /accountMailing/find）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "查询邮寄地址", description = "POST /wallet/mailing/find")
public class WalletMailingAddressFindRequest extends PageQueryHelperEntity {

	@ApiModelProperty(value = "邮寄地区 id")
	private Integer countryRegionId;

	@ApiModelProperty(value = "国家")
	private String country;

	@ApiModelProperty(value = "收件人")
	private String receiverName;
}
