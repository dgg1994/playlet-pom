package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 管理端发放优惠券入参。
 */
@Data
@ApiModel(value = "发放优惠券", description = "按模板向指定用户发放")
public class CouponIssueQuery {

	@ApiModelProperty(value = "券模板ID", required = true)
	private Integer templateId;

	@ApiModelProperty(value = "目标用户 uid 列表", required = true)
	private List<Integer> uidList;

	@ApiModelProperty(value = "备注")
	private String remark;
}
