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
 * 钱包用户 U 卡。
 */
@Data
@TableName("wallet_bankcard")
@ApiModel(value = "钱包U卡", description = "用户已开 U 卡")
public class WalletBankcardEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("card_apply_id")
	@ApiModelProperty(name = "cardApplyId", value = "wallet_card_apply.id")
	private Long cardApplyId;

	@TableField("card_product_id")
	@ApiModelProperty(name = "cardProductId", value = "卡产品 id")
	private Integer cardProductId;

	@TableField("card_uuid")
	@ApiModelProperty(name = "cardUuid", value = "卡产品 uuid")
	private String cardUuid;

	@TableField("user_bankcard_id")
	@ApiModelProperty(name = "userBankcardId", value = "对方 userBankcardId")
	private Long userBankcardId;

	@TableField("card_no")
	@ApiModelProperty(name = "cardNo", value = "卡号掩码")
	private String cardNo;

	@TableField("card_type")
	@ApiModelProperty(name = "cardType", value = "卡类型")
	private String cardType;

	@TableField("card_brand")
	@ApiModelProperty(name = "cardBrand", value = "MASTER / VISA")
	private String cardBrand;

	@TableField("bankcard_nature")
	@ApiModelProperty(name = "bankcardNature", value = "PHYSICAL / VIRTUAL")
	private String bankcardNature;

	@TableField("currency")
	@ApiModelProperty(name = "currency", value = "币种")
	private String currency;

	@TableField("card_status")
	@ApiModelProperty(name = "cardStatus", value = "卡状态索引 0-9")
	private Integer cardStatus;

	@TableField("card_status_name")
	@ApiModelProperty(name = "cardStatusName", value = "卡状态名 ACTIVE/FREEZE...")
	private String cardStatusName;

	@TableField("balance")
	@ApiModelProperty(name = "balance", value = "卡余额缓存")
	private BigDecimal balance;

	@TableField("pin_set")
	@ApiModelProperty(name = "pinSet", value = "是否已设 PIN")
	private Integer pinSet;

	@TableField("tag_name")
	@ApiModelProperty(name = "tagName", value = "自定义标签")
	private String tagName;

	@TableField("is_default")
	@ApiModelProperty(name = "isDefault", value = "默认提现卡")
	private Integer isDefault;

	@TableField("shipping_state")
	@ApiModelProperty(name = "shippingState", value = "实体卡发货状态")
	private Integer shippingState;

	@TableField("logistics_num")
	@ApiModelProperty(name = "logisticsNum", value = "物流单号")
	private String logisticsNum;

	@TableField("apply_order_no")
	@ApiModelProperty(name = "applyOrderNo", value = "开卡幂等单号")
	private String applyOrderNo;

	@TableField("fail_reason")
	@ApiModelProperty(name = "failReason", value = "失败原因")
	private String failReason;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
