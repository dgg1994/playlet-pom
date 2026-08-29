package com.playlet.internal.api.request;

import com.playlet.internal.api.response.WalletCardProductLabelResp;
import com.playlet.internal.api.response.WalletCardProductSynopsisResp;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端维护卡产品本地展示字段。
 */
@Data
@ApiModel(value = "维护卡产品请求", description = "更新展示图、上下架、热门、卡标签、卡简介")
public class WalletCardProductUpdateRequest {

	@NotNull(message = "产品 id 不能为空")
	@ApiModelProperty(value = "三方卡产品 id", required = true)
	private Integer id;

	@ApiModelProperty("卡片展示图 URL")
	private String cardImg;

	@ApiModelProperty("是否可申请：1是 0否")
	private Integer enable;

	@ApiModelProperty("是否热门：1是 0否")
	private Integer hot;

	@ApiModelProperty("卡名称")
	private String cardTitle;

	@ApiModelProperty("卡标签列表；传空数组清空标签；null 表示不改")
	private List<WalletCardProductLabelResp> labelList;

	@ApiModelProperty("卡简介；null 表示不改，传对象则可更新 title/content")
	private WalletCardProductSynopsisResp synopsisData;
}
