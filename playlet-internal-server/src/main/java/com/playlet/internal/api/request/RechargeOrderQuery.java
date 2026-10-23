package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端充值订单查询入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "充值订单查询", description = "统一查询会员/单集解锁现金订单")
public class RechargeOrderQuery extends PageQueryHelperEntity {

	@ApiModelProperty("关键词：订单号或用户ID")
	private String keyword;

	@ApiModelProperty("商品类型 MEMBER / EPISODE；空=全部")
	private String productType;

	@ApiModelProperty("支付方式 WECHAT / ALIPAY / BANK / COIN；空=全部")
	private String payChannel;

	@ApiModelProperty("支付状态 0待支付 1已支付 2已关闭 3已退款；空=全部")
	private Integer payStatus;
}
