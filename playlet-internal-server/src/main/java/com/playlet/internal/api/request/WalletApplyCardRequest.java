package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 申请开卡入参（对齐 onetoken CardApplyQueryEntity / openCardApply）。
 */
@Data
@ApiModel(value = "申请开卡", description = "选择卡产品后申请；需支付密码与持卡人；实体卡需邮寄地址；可选 KYC 快照")
public class WalletApplyCardRequest {

	@ApiModelProperty(value = "卡产品 id（来自 /card/findList 的 productId）", required = true)
	private Integer productId;

	@ApiModelProperty(value = "支付密码（6 位数字；须已绑定）", required = true)
	private String payPassword;

	@ApiModelProperty(value = "商户申请单号（幂等，前端生成；不传则由服务端生成）")
	private String requestOrderId;

	@ApiModelProperty(value = "充值方式：1 钱包余额 2 银行卡；默认 1")
	private Integer topupType;

	@ApiModelProperty(value = "已有持卡人 id（与 holderData 二选一）")
	private Long holderId;

	@ApiModelProperty(value = "持卡人信息（holderId 为空时必传）")
	private WalletCardHolderRequest holderData;

	@ApiModelProperty(value = "邮寄地址（实体卡必传，本地落库）")
	private WalletCardMailingAddressRequest mailingAddress;

	@ApiModelProperty(value = "开卡 KYC 证件快照；不传则从账户已通过 KYC 回填")
	private WalletCardApplyKycRequest kycData;

	@ApiModelProperty(value = "三方邮寄地址 id，通过添加邮寄地址接口获得，申请实体卡时可传")
	private Integer deliveryAddressId;

	@ApiModelProperty(value = "开卡费（可选；不传则取卡产品 applyFee）")
	private BigDecimal openCardCost;

	@ApiModelProperty(value = "邮费（实体卡；不传则取卡产品 logisticsMonery，默认 0）")
	private BigDecimal logisticsMonery;
}
