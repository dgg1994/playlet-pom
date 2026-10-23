package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 管理端同意/拒绝退款申请入参。
 */
@Data
@ApiModel(value = "退款申请处理", description = "同意或拒绝退款申请")
public class RefundApplyHandleQuery {

	@ApiModelProperty(value = "申请主键ID", required = true)
	private Integer id;

	@ApiModelProperty("处理备注")
	private String remark;
}
