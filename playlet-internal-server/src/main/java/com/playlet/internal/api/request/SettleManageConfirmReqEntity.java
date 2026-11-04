package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 确认单笔结算请求
 */
@Data
@ApiModel("确认单笔结算")
public class SettleManageConfirmReqEntity {

    @NotNull(message = "结算单 ID 不能为空")
    @ApiModelProperty(value = "结算单主键 ID", required = true, example = "1")
    private Long id;
}
