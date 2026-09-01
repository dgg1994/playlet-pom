package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 商户可用卡产品（申请开卡前选品）。
 */
@Data
@ApiModel(value = "可用卡产品", description = "供申请卡片时选择")
public class WalletCardProductItemResp {

	@ApiModelProperty("产品id，申请开卡时传 productId")
	private Integer productId;

	@ApiModelProperty("卡名称")
	private String cardTitle;

	@ApiModelProperty("关联 BIN")
	private String cardBin;

	@ApiModelProperty("VIRTUAL 虚拟卡 / PHYSICAL 实体卡")
	private String bankCardNature;

	@ApiModelProperty("卡品牌，如 VISA")
	private String cardBrand;

	@ApiModelProperty("NORMAL / SHARE")
	private String cardMode;

	@ApiModelProperty("消费币种")
	private String currency;

	@ApiModelProperty("开卡费用（美元）")
	private Double openCardCost;

	@ApiModelProperty("预存费用（美元）")
	private Double preSaveCost;

	@ApiModelProperty("月服务费（美元）")
	private Double monthFee;

	@ApiModelProperty("卡最大余额（美元）")
	private Double maxBalance;

	@ApiModelProperty("实体卡邮费（美元）；申请时不传则取此默认值")
	private Double logisticsMonery;

	@ApiModelProperty("开卡费用（兼容字段，同 openCardCost 整数部分）")
	private Integer applyFee;

	@ApiModelProperty("充值手续费比例 0-1")
	private Double rechargeFee;

	@ApiModelProperty("卡片区域，如 SGP")
	private String bankcardRegion;

	@ApiModelProperty("虚拟卡激活首次充值最小金额")
	private Integer activeMinLimit;

	@ApiModelProperty("单笔充值最小金额")
	private Integer rechargeMinLimit;

	@ApiModelProperty("卡片展示图 URL")
	private String cardImg;

	@ApiModelProperty("卡标签列表（对齐 worldpay lableList）")
	private List<WalletCardProductLabelResp> labelList;

	@ApiModelProperty("卡简介（对齐 worldpay synopsisData）")
	private WalletCardProductSynopsisResp synopsisData;
}
