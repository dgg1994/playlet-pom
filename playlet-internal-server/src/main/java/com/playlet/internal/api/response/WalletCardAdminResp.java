package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.playlet.internal.api.response.WalletCardProductLabelResp;
import com.playlet.internal.api.response.WalletCardProductSynopsisResp;

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

	@ApiModelProperty("卡名称")
	private String title;

	@ApiModelProperty("卡片类型 PHYSICAL / VIRTUAL")
	private String bankCardNature;

	@ApiModelProperty("卡片类型（同 bankCardNature，兼容前端 cardType）")
	private String cardType;

	@ApiModelProperty("卡币种")
	private String currency;

	@ApiModelProperty("月费")
	private BigDecimal monthFee;

	@ApiModelProperty("卡号段 / BIN")
	private String cardBin;

	@ApiModelProperty("卡品牌")
	private String cardBrand;

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
