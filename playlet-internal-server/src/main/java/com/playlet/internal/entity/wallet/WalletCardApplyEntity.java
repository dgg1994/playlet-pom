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
 * 钱包 U 卡开卡申请。
 */
@Data
@TableName("wallet_card_apply")
@ApiModel(value = "钱包开卡申请", description = "U 卡开卡申请单")
public class WalletCardApplyEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("holder_id")
	@ApiModelProperty(name = "holderId", value = "wallet_user_holder.id")
	private Long holderId;

	@TableField("card_product_id")
	@ApiModelProperty(name = "cardProductId", value = "卡产品 id")
	private Integer cardProductId;

	@TableField("card_uuid")
	@ApiModelProperty(name = "cardUuid", value = "卡产品 uuid")
	private String cardUuid;

	@TableField("card_type")
	@ApiModelProperty(name = "cardType", value = "卡类型文案")
	private String cardType;

	@TableField("topup_type")
	@ApiModelProperty(name = "topupType", value = "1钱包 2银行卡")
	private Integer topupType;

	@TableField("apply_state")
	@ApiModelProperty(name = "applyState", value = "开卡状态")
	private Integer applyState;

	@TableField("apply_state_name")
	@ApiModelProperty(name = "applyStateName", value = "开卡状态文案")
	private String applyStateName;

	@TableField("kyc_state")
	@ApiModelProperty(name = "kycState", value = "申请时 KYC 快照")
	private Integer kycState;

	@TableField("kyc_state_name")
	@ApiModelProperty(name = "kycStateName", value = "申请时 KYC 状态文案")
	private String kycStateName;

	@TableField("kyc_audit_result")
	@ApiModelProperty(name = "kycAuditResult", value = "申请时 KYC 结果描述")
	private String kycAuditResult;

	@TableField("open_card_cost")
	@ApiModelProperty(name = "openCardCost", value = "开卡费")
	private BigDecimal openCardCost;

	@TableField("pre_save_cost")
	@ApiModelProperty(name = "preSaveCost", value = "预存费")
	private BigDecimal preSaveCost;

	@TableField("open_card_total")
	@ApiModelProperty(name = "openCardTotal", value = "开卡总费用")
	private BigDecimal openCardTotal;

	@TableField("request_order_id")
	@ApiModelProperty(name = "requestOrderId", value = "申请幂等单号")
	private String requestOrderId;

	@TableField("reject_info")
	@ApiModelProperty(name = "rejectInfo", value = "拒绝原因")
	private String rejectInfo;

	@TableField("shipping_state")
	@ApiModelProperty(name = "shippingState", value = "实体卡发货状态")
	private Integer shippingState;

	@TableField("shipping_state_name")
	@ApiModelProperty(name = "shippingStateName", value = "发货状态文案")
	private String shippingStateName;

	@TableField("logistics_num")
	@ApiModelProperty(name = "logisticsNum", value = "物流单号")
	private String logisticsNum;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "申请时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
