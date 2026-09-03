package com.playlet.oversea.entity.wallet;

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
 * 实体卡发货记录。
 */
@Data
@TableName("wallet_card_shipping")
@ApiModel(value = "实体卡发货记录", description = "对齐 worldpay card_shipping")
public class WalletCardShippingEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("apply_id")
	@ApiModelProperty("申请单 id")
	private Long applyId;

	@TableField("wallet_user_id")
	private Long walletUserId;

	@TableField("wallet_uid")
	private Long walletUid;

	@TableField("user_email")
	private String userEmail;

	@TableField("user_name")
	private String userName;

	@TableField("user_tel")
	private String userTel;

	@TableField("logistics_providers")
	private String logisticsProviders;

	@TableField("logistics_num")
	private String logisticsNum;

	@TableField("logistics_monery")
	private BigDecimal logisticsMonery;

	@TableField("logistics_state")
	private Integer logisticsState;

	@TableField("logistics_state_name")
	private String logisticsStateName;

	@TableField("operate_user_id")
	private Integer operateUserId;

	@TableField("operate_user_name")
	private String operateUserName;

	@TableField("operate_user_ip")
	private String operateUserIp;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
