package com.playlet.oversea.api.response;

import com.playlet.oversea.entity.wallet.WalletCardApplyManEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理端用户持卡列表项（对齐 onetoken AppCardAccountCardEntity 字段名）。
 */
@Data
@ApiModel(value = "管理端用户持卡", description = "pcFindUserCardList / findUserCardList")
public class WalletBankcardAdminResp {

	private Long id;
	private String uid;
	private Integer cardId;
	private String cardUuid;
	private Long applyId;
	private String cardType;
	private String cardNo;
	private Long userBankcardId;
	private Integer status;
	private String statusName;
	private BigDecimal balance;
	private String tagName;
	private String userEmail;
	private String userTel;
	private String userName;
	private Date setTime;

	@ApiModelProperty("卡产品信息")
	private Object cardData;

	@ApiModelProperty("持卡人信息（开卡申请快照）")
	private WalletCardApplyManEntity manData;
}
