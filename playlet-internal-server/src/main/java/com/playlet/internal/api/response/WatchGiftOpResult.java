package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("观影礼操作结果")
public class WatchGiftOpResult {

	private boolean ok;
	private String msgKey;
	private WatchGiftHomeSummaryEntity summary;
	private Integer rewardCoin;

	public static WatchGiftOpResult fail(String msgKey) {
		WatchGiftOpResult r = new WatchGiftOpResult();
		r.setOk(false);
		r.setMsgKey(msgKey);
		return r;
	}

	public static WatchGiftOpResult success(WatchGiftHomeSummaryEntity summary, Integer rewardCoin) {
		WatchGiftOpResult r = new WatchGiftOpResult();
		r.setOk(true);
		r.setSummary(summary);
		r.setRewardCoin(rewardCoin == null ? 0 : rewardCoin);
		return r;
	}
}
