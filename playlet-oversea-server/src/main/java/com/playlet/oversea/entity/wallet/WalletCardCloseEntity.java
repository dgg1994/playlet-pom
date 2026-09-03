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

/**
 * 销卡申请记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wallet_card_close")
@ApiModel(value = "销卡记录", description = "用户销卡申请及审核状态")
public class WalletCardCloseEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("wallet_user_id")
	private Long walletUserId;

	@TableField("wallet_uid")
	private Long walletUid;

	@TableField("card_product_id")
	private Integer cardProductId;

	@TableField("card_uuid")
	private String cardUuid;

	@TableField("card_type")
	private String cardType;

	@TableField("card_no")
	private String cardNo;

	@TableField("user_bankcard_id")
	private Long userBankcardId;

	@TableField("balance")
	private BigDecimal balance;

	@TableField("refund_amt")
	private BigDecimal refundAmt;

	@TableField("request_order_id")
	private String requestOrderId;

	@TableField("review_status")
	private Integer reviewStatus;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;

	@TableField(exist = false)
	private String userEmail;

	@TableField(exist = false)
	private String reviewStatusName;
}
