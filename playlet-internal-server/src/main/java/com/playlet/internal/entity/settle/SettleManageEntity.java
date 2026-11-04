package com.playlet.internal.entity.settle;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 分账结算单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("settle_manage")
@ApiModel(value = "分账结算单")
public class SettleManageEntity extends PageQueryHelperEntity {

    @TableId(type = IdType.AUTO)
    @ApiModelProperty(name = "id", value = "主键", dataType = "Long")
    private Long id;

    @TableField("settle_no")
    @ApiModelProperty(name = "settleNo", value = "结算单号", dataType = "String")
    private String settleNo;

    @TableField("partner_id")
    @ApiModelProperty(name = "partnerId", value = "内容方 ID", dataType = "Integer")
    private Integer partnerId;

    @TableField("partner")
    @ApiModelProperty(name = "partner", value = "内容方名称", dataType = "String")
    private String partner;

    @TableField("drama_id")
    @ApiModelProperty(name = "dramaId", value = "短剧 ID", dataType = "Integer")
    private Integer dramaId;

    @TableField("drama_name")
    @ApiModelProperty(name = "dramaName", value = "短剧名称", dataType = "String")
    private String dramaName;

    @TableField("amount")
    @ApiModelProperty(name = "amount", value = "结算金额（元）", dataType = "BigDecimal")
    private BigDecimal amount;

    @TableField("creator_ratio")
    @ApiModelProperty(name = "creatorRatio", value = "作者分成 %（整数）", dataType = "Integer")
    private Integer creatorRatio;

    @TableField("platform_ratio")
    @ApiModelProperty(name = "platformRatio", value = "平台分成 %（整数，与作者之和=100）", dataType = "Integer")
    private Integer platformRatio;

    @TableField("period_start")
    @ApiModelProperty(name = "periodStart", value = "周期开始 yyyy-MM-dd", dataType = "Date")
    private Date periodStart;

    @TableField("period_end")
    @ApiModelProperty(name = "periodEnd", value = "周期结束 yyyy-MM-dd", dataType = "Date")
    private Date periodEnd;

    @TableField("status")
    @ApiModelProperty(name = "status", value = "0 待结算 1 已结算", dataType = "Integer")
    private Integer status;

    @TableField("settle_time")
    @ApiModelProperty(name = "settleTime", value = "实际结算时间", dataType = "Date")
    private Date settleTime;

    @TableField("remark")
    @ApiModelProperty(name = "remark", value = "运营备注", dataType = "String")
    private String remark;

    @TableField("setTime")
    @ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
    private Date setTime;

    @TableField("gmtModified")
    @ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
    private Date gmtModified;

    /** 列表搜索关键字（不持久化） */
    @TableField(exist = false)
    @ApiModelProperty(name = "keyword", value = "模糊搜：结算单号 / 内容方 / 短剧名", dataType = "String")
    private String keyword;
}
