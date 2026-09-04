package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端卡产品维护入参（对齐 onetoken CardEntity 字段名）。
 */
@Data
@ApiModel(value = "管理端卡产品更新", description = "POST /card/update")
public class WalletCardAdminUpdateRequest {

	@ApiModelProperty(value = "三方 card_id / 本地主键", required = true)
	private Integer id;

	@ApiModelProperty(value = "废弃：请用 id", hidden = true)
	private String uuid;

	@ApiModelProperty("卡名称")
	private String title;

	@ApiModelProperty("PHYSICAL / VIRTUAL")
	private String bankCardNature;

	private BigDecimal openCardCost;
	private BigDecimal preSaveCost;
	private BigDecimal rechargeFee;
	private Integer activeMinLimit;
	private Integer rechargeMinLimit;
	private BigDecimal rechargeMaxLimit;

	@ApiModelProperty("标签 id 列表")
	private List<Integer> lableIdList;

	@ApiModelProperty("简介 id 列表")
	private List<Integer> synopsisIdList;
}
