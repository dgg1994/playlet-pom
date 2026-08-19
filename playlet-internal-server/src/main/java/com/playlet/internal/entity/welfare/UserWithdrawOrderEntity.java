package com.playlet.internal.entity.welfare;

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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_withdraw_order")
@ApiModel(value = "提现订单", description = "用户提现订单")
public class UserWithdrawOrderEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("order_no")
	@ApiModelProperty(name = "orderNo", value = "业务单号", dataType = "String")
	private String orderNo;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户uid", dataType = "Integer")
	private Integer uid;

	@TableField("user_type")
	@ApiModelProperty(name = "userType", value = "主体 1 C端 2 作家", dataType = "Integer")
	private Integer userType;

	@TableField("asset_code")
	@ApiModelProperty(name = "assetCode", value = "币种编码", dataType = "String")
	private String assetCode;

	@TableField("network")
	@ApiModelProperty(name = "network", value = "网络", dataType = "String")
	private String network;

	@TableField("wallet_address")
	@ApiModelProperty(name = "walletAddress", value = "收款地址", dataType = "String")
	private String walletAddress;

	@TableField("points_amt")
	@ApiModelProperty(name = "pointsAmt", value = "扣减积分", dataType = "Integer")
	private Integer pointsAmt;

	@TableField("rate")
	@ApiModelProperty(name = "rate", value = "下单时 points_per_unit 快照", dataType = "Integer")
	private Integer rate;

	@TableField("fee_amt")
	@ApiModelProperty(name = "feeAmt", value = "手续费", dataType = "BigDecimal")
	private BigDecimal feeAmt;

	@TableField("gross_amt")
	@ApiModelProperty(name = "grossAmt", value = "毛额", dataType = "BigDecimal")
	private BigDecimal grossAmt;

	@TableField("actual_amt")
	@ApiModelProperty(name = "actualAmt", value = "实到", dataType = "BigDecimal")
	private BigDecimal actualAmt;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "0待处理 1打款中 2成功 3失败 4已退回", dataType = "Integer")
	private Integer status;

	@TableField("tx_hash")
	@ApiModelProperty(name = "txHash", value = "链上哈希", dataType = "String")
	private String txHash;

	@TableField("fail_reason")
	@ApiModelProperty(name = "failReason", value = "失败原因", dataType = "String")
	private String failReason;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
