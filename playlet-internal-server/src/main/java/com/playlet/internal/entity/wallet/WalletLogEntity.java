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
import java.util.List;

/**
 * 钱包账变记录。
 */
@Data
@TableName("wallet_log")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "钱包账变记录", description = "钱包收入/支出流水")
public class WalletLogEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("order_no")
	@ApiModelProperty(name = "orderNo", value = "订单号")
	private String orderNo;

	@TableField("out_order_no")
	@ApiModelProperty(name = "outOrderNo", value = "外部订单号")
	private String outOrderNo;

	@TableField("wallet_user_id")
	@ApiModelProperty(name = "walletUserId", value = "wallet_user.id")
	private Long walletUserId;

	@TableField("wallet_uid")
	@ApiModelProperty(name = "walletUid", value = "钱包三方 uid")
	private Long walletUid;

	@TableField("trade_type")
	@ApiModelProperty(name = "tradeType", value = "1进账 2出账")
	private Integer tradeType;

	@TableField("title")
	@ApiModelProperty(name = "title", value = "标题")
	private String title;

	@TableField("primeval_money")
	@ApiModelProperty(name = "primevalMoney", value = "变动前余额")
	private BigDecimal primevalMoney;

	@TableField("primeval_money_unit")
	@ApiModelProperty(name = "primevalMoneyUnit", value = "币种")
	private String primevalMoneyUnit;

	@TableField("real_money")
	@ApiModelProperty(name = "realMoney", value = "实际金额")
	private BigDecimal realMoney;

	@TableField("service_charge")
	@ApiModelProperty(name = "serviceCharge", value = "手续费")
	private BigDecimal serviceCharge;

	@TableField("form_name")
	@ApiModelProperty(name = "formName", value = "出账方名称")
	private String formName;

	@TableField("form_account")
	@ApiModelProperty(name = "formAccount", value = "出账方账户")
	private String formAccount;

	@TableField("to_name")
	@ApiModelProperty(name = "toName", value = "入账方名称")
	private String toName;

	@TableField("to_account")
	@ApiModelProperty(name = "toAccount", value = "入账方账户")
	private String toAccount;

	@TableField("memo")
	@ApiModelProperty(name = "memo", value = "备注")
	private String memo;

	@TableField("network_type")
	@ApiModelProperty(name = "networkType", value = "网络类型")
	private String networkType;

	@TableField("wallet_bankcard_id")
	@ApiModelProperty(name = "walletBankcardId", value = "关联银行卡")
	private Long walletBankcardId;

	@TableField("operate_type")
	@ApiModelProperty(name = "operateType", value = "操作类型")
	private Integer operateType;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "状态")
	private String status;

	@TableField("tran_hash")
	@ApiModelProperty(name = "tranHash", value = "链上 hash")
	private String tranHash;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;

	@TableField("setUser")
	@ApiModelProperty(name = "setUser", value = "操作人")
	private Integer setUser;

	@TableField("setUserName")
	@ApiModelProperty(name = "setUserName", value = "操作人姓名")
	private String setUserName;

	@TableField(exist = false)
	@ApiModelProperty(name = "userEmail", value = "用户邮箱")
	private String userEmail;

	@TableField(exist = false)
	@ApiModelProperty(name = "operateTypeList", value = "操作类型筛选")
	private List<Integer> operateTypeList;

	@TableField(exist = false)
	@ApiModelProperty(name = "yearsMonth", value = "月份 yyyy-MM")
	private String yearsMonth;
}
