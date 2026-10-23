package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端退款申请列表入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "退款申请查询", description = "退款管理列表筛选")
public class RefundApplyQuery extends PageQueryHelperEntity {

	@ApiModelProperty("关键词：原订单号 / 用户昵称 / 用户ID")
	private String keyword;

	@ApiModelProperty("状态 0待处理 1已退款 2已拒绝；空=全部")
	private Integer status;
}
