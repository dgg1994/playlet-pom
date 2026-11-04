package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端入驻申请列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "入驻申请审核查询", description = "名称/联系人/联系方式模糊；入驻性质、审核状态筛选")
public class CreatorSettleApplyQuery extends PageQueryHelperEntity {

    @ApiModelProperty(value = "关键词：入驻名称/联系人/联系方式")
    private String keyword;

    @ApiModelProperty(value = "入驻性质：1个人 2企业/团队")
    private Integer identityType;

    @ApiModelProperty(value = "状态：0待查看 1已查看 2已通过 3已驳回")
    private Integer auditStatus;
}
