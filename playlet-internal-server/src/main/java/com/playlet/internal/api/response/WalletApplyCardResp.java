package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 申请开卡结果（对齐 worldpay 申请开卡 + 自动发卡）。
 */
@Data
@ApiModel(value = "申请开卡结果", description = "本地申请单 + 三方卡信息")
public class WalletApplyCardResp {

	@ApiModelProperty("本地申请单 id")
	private Long applyId;

	@ApiModelProperty("持卡人 id")
	private Long holderId;

	@ApiModelProperty("商户申请单号")
	private String requestOrderId;

	@ApiModelProperty("卡产品 id")
	private Integer productId;

	@ApiModelProperty("VIRTUAL / PHYSICAL")
	private String cardType;

	@ApiModelProperty("申请状态码")
	private Integer applyState;

	@ApiModelProperty("申请状态文案")
	private String applyStateName;

	@ApiModelProperty("三方订单号")
	private String orderNo;

	@ApiModelProperty("对方 userBankcardId")
	private Long userBankcardId;

	@ApiModelProperty("卡号（申请时可能为空）")
	private String cardNo;

	@ApiModelProperty("本地 wallet_bankcard.id（已落库时）")
	private Long walletBankcardId;

	@ApiModelProperty("虚拟卡且 KYC 已通过时是否已自动发起三方开卡")
	private Boolean autoIssued;

	@ApiModelProperty("申请时 KYC 状态码")
	private Integer kycState;

	@ApiModelProperty("申请时 KYC 状态文案")
	private String kycStateName;

	@ApiModelProperty("KYC 未通过时为 true，需调用 /wallet/kyc/applyByCardApply 或 /wallet/kyc/apply")
	private Boolean kycSubmitRequired;
}
