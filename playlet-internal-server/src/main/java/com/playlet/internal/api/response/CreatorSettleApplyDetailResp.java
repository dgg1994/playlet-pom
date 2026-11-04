package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 入驻申请详情。
 */
@Data
@ApiModel("入驻申请详情")
public class CreatorSettleApplyDetailResp {

    @ApiModelProperty("申请记录ID（creator_profile.id）")
    private Integer applyId;

    @ApiModelProperty("申请ID，如 RZ2026001")
    private String applyNo;

    @ApiModelProperty("入驻名称")
    private String applyName;

    @ApiModelProperty("入驻性质：1个人 2企业/团队")
    private Integer identityType;

    @ApiModelProperty("入驻身份：1出品方 2MCN机构 3独立创作者")
    private Integer creatorRole;

    @ApiModelProperty("真实姓名/法人姓名")
    private String realName;

    @ApiModelProperty("机构名称")
    private String orgName;

    @ApiModelProperty("证件正面图")
    private String idCardFront;

    @ApiModelProperty("证件背面图")
    private String idCardBack;

    @ApiModelProperty("营业执照图")
    private String orgLicense;

    @ApiModelProperty("账单寄送地址")
    private String billAddress;

    @ApiModelProperty("资历自述")
    private String qualifyDesc;

    @ApiModelProperty("联系人")
    private String contactName;

    @ApiModelProperty("联系方式")
    private String contactPhone;

    @ApiModelProperty("登录邮箱")
    private String userAccount;

    @ApiModelProperty("申请时间")
    private Date applyTime;

    @ApiModelProperty("状态：0待查看 1已查看 2已通过 3已驳回")
    private Integer auditStatus;

    @ApiModelProperty("驳回原因")
    private String auditRejectReason;

    @ApiModelProperty("通过时间")
    private Date auditPassTime;
}
