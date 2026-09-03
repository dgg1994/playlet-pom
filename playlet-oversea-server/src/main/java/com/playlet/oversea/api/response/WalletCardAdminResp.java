package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.playlet.oversea.api.response.WalletCardProductLabelResp;
import com.playlet.oversea.api.response.WalletCardProductSynopsisResp;

/**
 * 管理端卡产品列表项（对齐 onetoken CardEntity 字段名）。
 */
@Data
@ApiModel(value = "管理端卡产品", description = "findListPag 列表项")
public class WalletCardAdminResp {

	@ApiModelProperty("三方 card_id")
	private Integer id;

	@ApiModelProperty("卡产品 uuid")
	private String uuid;

	private String title;
	private String bankCardNature;
	private String img;
	private String listImg;
	private BigDecimal openCardCost;
	private BigDecimal preSaveCost;
	private BigDecimal rechargeFee;
	private Integer activeMinLimit;
	private Integer rechargeMinLimit;
	private BigDecimal rechargeMaxLimit;
	private Integer enable;
	private Integer hot;

	private List<WalletCardProductLabelResp> lableList;
	private List<Integer> lableIdList;
	private WalletCardProductSynopsisResp synopsisData;
	private List<Integer> synopsisIdList;
}
