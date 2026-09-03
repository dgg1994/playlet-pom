package com.playlet.oversea.entity.wallet;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.playlet.oversea.api.response.WalletCardProductLabelResp;
import com.playlet.oversea.api.response.WalletCardProductSynopsisResp;

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

	@TableField("recharge_max_limit")
	@ApiModelProperty(name = "rechargeMaxLimit", value = "卡最大余额")
	private BigDecimal rechargeMaxLimit;

	@TableField("logistics_monery")
	@ApiModelProperty(name = "logisticsMonery", value = "实体卡邮费（申请默认）")
	private BigDecimal logisticsMonery;

	@TableField("currency")
	@ApiModelProperty(name = "currency", value = "币种")
	private String currency;

	@TableField("apply_fee")
	@ApiModelProperty(name = "applyFee", value = "开卡费")
	private BigDecimal applyFee;

	@TableField("open_card_cost")
	@ApiModelProperty(name = "openCardCost", value = "开卡费用（展示/冻结；空则回退 applyFee）")
	private BigDecimal openCardCost;

	@TableField("pre_save_cost")
	@ApiModelProperty(name = "preSaveCost", value = "预存费用")
	private BigDecimal preSaveCost;

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

	@TableField("card_list_img")
	@ApiModelProperty(name = "cardListImg", value = "列表展示图")
	private String cardListImg;

	@TableField("description1")
	@ApiModelProperty(name = "description1", value = "卡简介正文（映射 synopsisData.content）")
	private String description1;

	@TableField("description2")
	@ApiModelProperty(name = "description2", value = "卡标签（映射 labelList，多标签用 |/,/，分隔）")
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

	/** 管理端/C 端展示：卡标签列表（非表字段，由 description2 解析） */
	@TableField(exist = false)
	@ApiModelProperty(name = "labelList", value = "卡标签列表")
	private List<WalletCardProductLabelResp> labelList;

	/** 管理端/C 端展示：卡简介（非表字段，由 description1 + cardTitle 组装） */
	@TableField(exist = false)
	@ApiModelProperty(name = "synopsisData", value = "卡简介")
	private WalletCardProductSynopsisResp synopsisData;

	/** 管理端筛选：卡性质（对齐 onetoken bankCardNature） */
	@TableField(exist = false)
	@ApiModelProperty(name = "bankCardNature", value = "PHYSICAL/VIRTUAL 筛选别名")
	private String bankCardNatureFilter;

	/** 管理端筛选：标题（对齐 onetoken title） */
	@TableField(exist = false)
	private String title;

	/** 管理端编辑回显：关联的标签 id 列表（wallet_card_label_join） */
	@TableField(exist = false)
	@ApiModelProperty(name = "labelIdList", value = "关联标签 id 列表")
	private List<Integer> labelIdList;

	/** 管理端编辑回显：关联简介 id 列表 */
	@TableField(exist = false)
	private List<Integer> synopsisIdList;
}
