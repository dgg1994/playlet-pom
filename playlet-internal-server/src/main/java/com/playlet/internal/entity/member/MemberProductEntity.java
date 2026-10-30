package com.playlet.internal.entity.member;

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
 * 会员套餐。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_product")
@ApiModel(value = "会员套餐", description = "会员商品定义")
public class MemberProductEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("product_code")
	@ApiModelProperty(name = "productCode", value = "套餐编码", dataType = "String")
	private String productCode;

	@TableField("product_name")
	@ApiModelProperty(name = "productName", value = "展示名", dataType = "String")
	private String productName;

	@TableField("product_type")
	@ApiModelProperty(name = "productType", value = "1一次性 2连续包月", dataType = "Integer")
	private Integer productType;

	@TableField("duration_days")
	@ApiModelProperty(name = "durationDays", value = "有效天数", dataType = "Integer")
	private Integer durationDays;

	@TableField("renew_price_yuan")
	@ApiModelProperty(name = "renewPriceYuan", value = "连续包月每期续费价(元)；为空取 priceYuan", dataType = "BigDecimal")
	private BigDecimal renewPriceYuan;

	@TableField("renew_period_days")
	@ApiModelProperty(name = "renewPeriodDays", value = "连续包月每期天数；连续包月时为周期长度", dataType = "Integer")
	private Integer renewPeriodDays;

	@TableField("original_price")
	@ApiModelProperty(name = "originalPrice", value = "划线原价(元)", dataType = "BigDecimal")
	private BigDecimal originalPrice;

	@TableField("price_yuan")
	@ApiModelProperty(name = "priceYuan", value = "现金售价(元)", dataType = "BigDecimal")
	private BigDecimal priceYuan;

	@TableField("price_coin")
	@ApiModelProperty(name = "priceCoin", value = "金币售价；0表示不支持金币购买", dataType = "Integer")
	private Integer priceCoin;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "排序权重", dataType = "Integer")
	private Integer sortWeight;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1上架 0下架", dataType = "Integer")
	private Integer status;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("badge_text")
	@ApiModelProperty(name = "badgeText", value = "价格徽章（App 端价格下方小徽章，如：立省10元、超级福利）", dataType = "String")
	private String badgeText;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
