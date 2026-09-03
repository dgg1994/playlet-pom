package com.playlet.oversea.api.response;

import com.playlet.oversea.entity.wallet.WalletCardProductEntity;
import com.playlet.oversea.entity.wallet.WalletUserHolderEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * 持有银行卡详情（对齐 onetoken findUserCardInfo）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "钱包U卡详情", description = "持卡详情：卡信息、持卡人、卡产品、物流轨迹")
public class WalletCardDetailResp extends WalletCardItemResp {

	@ApiModelProperty("wallet_card_apply.id")
	private Long cardApplyId;

	@ApiModelProperty("钱包三方 uid")
	private Long walletUid;

	@ApiModelProperty("卡产品 uuid")
	private String cardUuid;

	@ApiModelProperty("实体卡发货状态")
	private Integer shippingState;

	@ApiModelProperty("发货状态文案")
	private String shippingStateName;

	@ApiModelProperty("发货时间")
	private Date shippingTime;

	@ApiModelProperty("物流单号")
	private String logisticsNum;

	@ApiModelProperty("持卡人资料")
	private WalletUserHolderEntity holderData;

	@ApiModelProperty("卡产品信息")
	private WalletCardProductEntity cardData;

	@ApiModelProperty("物流轨迹")
	private List<WalletLogisticsEventResp> logisticsInfo;

	@ApiModelProperty("月服务费，如 1.00")
	private String monthServiceFee;

	@ApiModelProperty("USD 充值手续费，如 1.0%")
	private String usdRechargeFee;

	@ApiModelProperty("USDT 充值手续费，如 1.0%")
	private String usdtRechargeFee;

	@ApiModelProperty("提现手续费，如 1.0%")
	private String withdrawFee;
}
