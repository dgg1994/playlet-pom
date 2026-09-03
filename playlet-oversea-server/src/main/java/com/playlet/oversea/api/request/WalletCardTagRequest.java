package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 修改持有银行卡自定义标签（对齐 onetoken UserCardTagEntity / POST /appUserCard/upTag）。
 */
@Data
@ApiModel(value = "修改银行卡标签", description = "POST /wallet/card/upTag")
public class WalletCardTagRequest {

	@ApiModelProperty(value = "用户银行卡 id（三方 userBankcardId）", required = true)
	private Long userBankcardId;

	@ApiModelProperty(value = "自定义标签；传空字符串可清空", required = true)
	private String tag;
}
