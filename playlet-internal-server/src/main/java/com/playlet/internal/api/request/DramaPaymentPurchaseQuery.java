package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短剧付费-剧集购买记录查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "短剧购买记录查询", description = "按短剧维度查购买明细")
public class DramaPaymentPurchaseQuery extends PageQueryHelperEntity {

	@ApiModelProperty(value = "短剧ID", required = true)
	private Integer dramaId;

	@ApiModelProperty("关键词：订单号 / 用户昵称 / 用户ID")
	private String keyword;

	@ApiModelProperty("集ID；空=全部集")
	private Integer assetId;

	@ApiModelProperty("支付方式 WECHAT / ALIPAY / BANK / COIN；空=全部")
	private String payChannel;

	@ApiModelProperty("支付状态 0待支付 1已支付 2已关闭 3已退款；空=全部")
	private Integer payStatus;
}
