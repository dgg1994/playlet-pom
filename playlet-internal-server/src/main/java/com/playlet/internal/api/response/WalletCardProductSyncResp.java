package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 卡产品三方同步结果。
 */
@Data
@ApiModel(value = "卡产品同步结果", description = "一键拉取三方卡产品")
public class WalletCardProductSyncResp {

	@ApiModelProperty("三方返回总数")
	private Integer total;

	@ApiModelProperty("新增条数")
	private Integer inserted;

	@ApiModelProperty("更新条数")
	private Integer updated;

	public static WalletCardProductSyncResp of(int total, int inserted, int updated) {
		WalletCardProductSyncResp resp = new WalletCardProductSyncResp();
		resp.setTotal(total);
		resp.setInserted(inserted);
		resp.setUpdated(updated);
		return resp;
	}
}
