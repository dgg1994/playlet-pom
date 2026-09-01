package com.playlet.internal.api.response;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包 U 卡列表项。
 */
@Data
@ApiModel(value = "钱包U卡列表项", description = "卡片列表展示字段")
public class WalletCardItemResp {

	@ApiModelProperty("本地 wallet_bankcard.id")
	private Long id;

	@ApiModelProperty("对方 userBankcardId")
	private Long userBankcardId;

	@ApiModelProperty("展示名称：优先 tagName，其次卡产品 cardTitle，否则品牌+尾号")
	private String displayName;

	@ApiModelProperty("卡产品名称（wallet_card_product.card_title）")
	private String cardTitle;

	@ApiModelProperty("卡号掩码")
	private String cardNo;

	@ApiModelProperty("卡品牌 MASTER/VISA")
	private String cardBrand;

	@ApiModelProperty(name = "cardProductId", value = "卡产品 id")
	private Integer cardProductId;

	@ApiModelProperty("PHYSICAL / VIRTUAL")
	private String bankcardNature;

	@ApiModelProperty("币种")
	private String currency;

	@ApiModelProperty("卡状态")
	private Integer cardStatus;

	@ApiModelProperty("卡状态文案")
	private String cardStatusName;

	@ApiModelProperty("卡余额缓存")
	private BigDecimal balance;

	@ApiModelProperty("是否默认卡 1是 0否（首页当前卡）")
	private Integer isDefault;

	@ApiModelProperty("是否已设 PIN")
	private Integer pinSet;

	@ApiModelProperty("自定义标签")
	private String tagName;

	@ApiModelProperty("卡片展示图 URL")
	private String cardImg;

	@ApiModelProperty("开卡时间")
	private Date setTime;
}
