package com.playlet.oversea.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("withdraw_config")
@ApiModel(value = "提现配置", description = "积分兑多币种规则")
public class WithdrawConfigEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("asset_code")
	@ApiModelProperty(name = "assetCode", value = "币种 TRX/USDT/USTC", dataType = "String")
	private String assetCode;

	@TableField("network")
	@ApiModelProperty(name = "network", value = "网络 TRC20/ERC20", dataType = "String")
	private String network;

	@TableField("points_per_unit")
	@ApiModelProperty(name = "pointsPerUnit", value = "多少积分=1单位币", dataType = "Integer")
	private Integer pointsPerUnit;

	@TableField("service_fee")
	@ApiModelProperty(name = "serviceFee", value = "手续费（币种单位）", dataType = "BigDecimal")
	private BigDecimal serviceFee;

	@TableField("min_withdraw_points")
	@ApiModelProperty(name = "minWithdrawPoints", value = "最低提现积分", dataType = "Integer")
	private Integer minWithdrawPoints;

	@TableField("max_withdraw_points_day")
	@ApiModelProperty(name = "maxWithdrawPointsDay", value = "单日上限积分，0不限", dataType = "Integer")
	private Integer maxWithdrawPointsDay;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0关闭", dataType = "Integer")
	private Integer status;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "展示排序", dataType = "Integer")
	private Integer sortWeight;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
