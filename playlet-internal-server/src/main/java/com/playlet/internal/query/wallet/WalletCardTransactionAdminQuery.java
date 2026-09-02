package com.playlet.internal.query.wallet;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端卡交易流水查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "卡交易查询", description = "pcFindTransaction 管理端")
public class WalletCardTransactionAdminQuery extends PageQueryHelperEntity {

	@ApiModelProperty("TOPUP / AUTH / CLOSE 等")
	private String transType;

	private String cardNo;
	private String requestOrderId;
	private String userEmail;

	@ApiModelProperty("C 端 uid")
	private String uid;

	private Long walletUid;
}
