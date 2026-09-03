package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 商户可用卡产品。
 */
@Data
@ApiModel(value = "卡产品", description = "GET /api/bankcard/merchant/card/list")
public class ThirdBankcardProductResp {

	@ApiModelProperty("商户appid")
	private String mchAppid;

	@ApiModelProperty("卡名称")
	private String cardTitle;

	@ApiModelProperty("卡片id（申请时 productId）")
	private Integer id;

	@ApiModelProperty("关联BIN码")
	private String cardBin;

	@ApiModelProperty("VIRTUAL / PHYSICAL")
	private String bankCardNature;

	@ApiModelProperty("卡品牌")
	private String cardBrand;

	@ApiModelProperty("NORMAL / SHARE")
	private String cardMode;

	@ApiModelProperty("消费币种")
	private String ccy;

	@ApiModelProperty("开卡费用")
	private Integer applyFee;

	@ApiModelProperty("充值手续费比例 0-1")
	private Double rechargeFee;

	@ApiModelProperty("卡片区域")
	private String bankcardRegion;

	@ApiModelProperty("虚拟卡激活首次充值最小金额")
	private Integer activeMinLimit;

	@ApiModelProperty("单笔充值最小金额")
	private Integer rechargeMinLimit;

	@ApiModelProperty("卡片展示图 URL")
	private String cardImg;

	@ApiModelProperty("开卡费用（部分渠道返回）")
	private Double openCardCost;

	@ApiModelProperty("预存费用（部分渠道返回）")
	private Double preSaveCost;

	@ApiModelProperty("月费（部分渠道返回）")
	private Double monthFee;

	@ApiModelProperty("卡最大余额/充值上限（部分渠道返回）")
	private Double rechargeMaxLimit;
}
