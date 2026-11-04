package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 入驻申请列表项。
 */
@Data
@ApiModel("入驻申请列表项")
public class CreatorSettleApplyItemEntity {

    @ApiModelProperty("申请记录ID（creator_profile.id）")
    private Integer applyId;

    @ApiModelProperty("申请ID，如 RZ2026001")
    private String applyNo;

    @ApiModelProperty("入驻名称：企业取机构名称，个人取真实姓名/昵称")
    private String applyName;

    @ApiModelProperty("入驻性质：1个人 2企业/团队")
    private Integer identityType;

    @ApiModelProperty("入驻身份：1出品方 2MCN机构 3独立创作者")
    private Integer creatorRole;

    @ApiModelProperty("联系人")
    private String contactName;

    @ApiModelProperty("联系方式（列表中脱敏）")
    private String contactPhone;

    @ApiModelProperty("申请时间")
    private Date applyTime;

    @ApiModelProperty("状态：0待查看 1已查看 2已通过 3已驳回")
    private Integer auditStatus;
}
