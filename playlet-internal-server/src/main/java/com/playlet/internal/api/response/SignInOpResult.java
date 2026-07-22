package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 签到/补签操作结果（领域层）；HTTP 层据此转 ResponseBase
 */
@Data
@ApiModel("签到操作结果")
public class SignInOpResult {

	@ApiModelProperty("是否成功")
	private boolean ok;

	@ApiModelProperty("失败时的 i18n key")
	private String msgKey;

	@ApiModelProperty("成功后的签到摘要")
	private SignInHomeSummaryEntity summary;

	@ApiModelProperty("本次到账金币")
	private Integer rewardCoin;

	@ApiModelProperty("本次扣费金币（补签）")
	private Integer costCoin;

	public static SignInOpResult fail(String msgKey) {
		SignInOpResult r = new SignInOpResult();
		r.setOk(false);
		r.setMsgKey(msgKey);
		return r;
	}

	public static SignInOpResult success(SignInHomeSummaryEntity summary, Integer rewardCoin, Integer costCoin) {
		SignInOpResult r = new SignInOpResult();
		r.setOk(true);
		r.setSummary(summary);
		r.setRewardCoin(rewardCoin);
		r.setCostCoin(costCoin == null ? 0 : costCoin);
		return r;
	}
}
