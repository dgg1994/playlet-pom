package com.playlet.internal.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 钱包 U 卡产品缓存。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_card_product")
@ApiModel(value = "钱包卡产品", description = "U 卡产品缓存，主键为三方 card_id")
public class WalletCardProductEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.INPUT)
	@ApiModelProperty(name = "id", value = "三方 card_id")
	private Integer id;

	@TableField("card_title")
	@ApiModelProperty(name = "cardTitle", value = "卡名称")
	private String cardTitle;

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

	@TableField("card_bin")
	@ApiModelProperty(name = "cardBin", value = "关联 BIN")
	private String cardBin;

	@TableField("card_mode")
	@ApiModelProperty(name = "cardMode", value = "NORMAL / SHARE")
	private String cardMode;

	@TableField("bankcard_region")
	@ApiModelProperty(name = "bankcardRegion", value = "卡片区域")
	private String bankcardRegion;

	@TableField("active_min_limit")
	@ApiModelProperty(name = "activeMinLimit", value = "虚拟卡激活首次充值最小金额")
	private Integer activeMinLimit;

	@TableField("recharge_min_limit")
	@ApiModelProperty(name = "rechargeMinLimit", value = "单笔充值最小金额")
	private Integer rechargeMinLimit;

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
	@ApiModelProperty(name = "enable", value = "是否可申请 1是 0否")
	private Integer enable;

	@TableField("hot")
	@ApiModelProperty(name = "hot", value = "是否热门 1是 0否")
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
