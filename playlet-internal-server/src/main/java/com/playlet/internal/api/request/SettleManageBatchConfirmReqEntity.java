package com.playlet.internal.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量确认结算请求
 */
@Data
@ApiModel("批量确认结算")
public class SettleManageBatchConfirmReqEntity {

    @NotEmpty(message = "结算单 ID 集合不能为空")
    @ApiModelProperty(value = "结算单主键 ID 列表", required = true, example = "[1,2]")
    private List<Long> ids;
}
