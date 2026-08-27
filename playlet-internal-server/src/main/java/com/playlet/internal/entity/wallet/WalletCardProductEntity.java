package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包 U 卡产品缓存。
 */
@Data
@TableName("wallet_card_product")
@ApiModel(value = "钱包卡产品", description = "U 卡产品缓存，主键为三方 card_id")
public class WalletCardProductEntity {

	@TableId(type = IdType.INPUT)
	@ApiModelProperty(name = "id", value = "三方 card_id")
	private Integer id;

	@TableField("product_uuid")
	@ApiModelProperty(name = "productUuid", value = "卡产品 uuid")
	private String productUuid;

	@TableField("bankcard_nature")
	@ApiModelProperty(name = "bankcardNature", value = "PHYSICAL / VIRTUAL")
	private String bankcardNature;

	@TableField("bankcard_type")
	@ApiModelProperty(name = "bankcardType", value = "MASTER / VISA")
	private String bankcardType;

	@TableField("card_brand")
	@ApiModelProperty(name = "cardBrand", value = "卡品牌")
	private String cardBrand;

	@TableField("currency")
	@ApiModelProperty(name = "currency", value = "币种")
	private String currency;

	@TableField("apply_fee")
	@ApiModelProperty(name = "applyFee", value = "开卡费")
	private BigDecimal applyFee;

	@TableField("apply_discount")
	@ApiModelProperty(name = "applyDiscount", value = "申请折扣")
	private Integer applyDiscount;

	@TableField("month_fee")
	@ApiModelProperty(name = "monthFee", value = "月费")
	private BigDecimal monthFee;

	@TableField("recharge_fee")
	@ApiModelProperty(name = "rechargeFee", value = "充值费率")
	private BigDecimal rechargeFee;

	@TableField("card_img")
	@ApiModelProperty(name = "cardImg", value = "展示图")
	private String cardImg;

	@TableField("description1")
	@ApiModelProperty(name = "description1", value = "描述1")
	private String description1;

	@TableField("description2")
	@ApiModelProperty(name = "description2", value = "描述2")
	private String description2;

	@TableField("enable")
	@ApiModelProperty(name = "enable", value = "是否可申请")
	private Integer enable;

	@TableField("hot")
	@ApiModelProperty(name = "hot", value = "是否热门")
	private Integer hot;

	@TableField("sync_time")
	@ApiModelProperty(name = "syncTime", value = "最近同步时间")
	private Date syncTime;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
