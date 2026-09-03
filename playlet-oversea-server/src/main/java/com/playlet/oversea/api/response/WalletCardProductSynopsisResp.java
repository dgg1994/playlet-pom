package com.playlet.oversea.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 卡产品简介
 */
@Data
@ApiModel(value = "卡产品简介", description = "申请页卡片简介")
public class WalletCardProductSynopsisResp {

	@ApiModelProperty("简介 id（本地无独立简介表时可为 null）")
	private Integer id;

	@ApiModelProperty("简介标题")
	private String title;

	@ApiModelProperty("简介内容")
	private String content;
}
