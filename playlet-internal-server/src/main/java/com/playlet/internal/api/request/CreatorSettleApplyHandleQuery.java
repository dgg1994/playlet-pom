package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 入驻申请审核入参。
 */
@Data
@ApiModel("入驻申请审核")
public class CreatorSettleApplyHandleQuery {

    @NotNull(message = "申请ID不能为空")
    @ApiModelProperty(value = "申请记录ID（creator_profile.id）", required = true)
    private Integer applyId;

    @NotNull(message = "审核结果不能为空")
    @ApiModelProperty(value = "是否通过：true通过 false驳回", required = true)
    private Boolean pass;

    @ApiModelProperty("驳回原因；pass=false 时必填")
    private String rejectReason;
}
