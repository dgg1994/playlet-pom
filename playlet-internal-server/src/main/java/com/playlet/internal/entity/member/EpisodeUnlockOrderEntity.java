package com.playlet.internal.entity.member;

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
 * 单集现金解锁订单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("episode_unlock_order")
@ApiModel(value = "单集解锁订单", description = "现金解锁剧集订单")
public class EpisodeUnlockOrderEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("order_no")
	@ApiModelProperty(name = "orderNo", value = "业务订单号", dataType = "String")
	private String orderNo;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "剧ID", dataType = "Integer")
	private Integer dramaId;

	@TableField("asset_id")
	@ApiModelProperty(name = "assetId", value = "集ID", dataType = "Integer")
	private Integer assetId;

	@TableField("pay_amount")
	@ApiModelProperty(name = "payAmount", value = "实付(元)", dataType = "BigDecimal")
	private BigDecimal payAmount;

	@TableField("pay_status")
	@ApiModelProperty(name = "payStatus", value = "0待支付 1已支付 2已关闭 3已退款", dataType = "Integer")
	private Integer payStatus;

	@TableField("pay_time")
	@ApiModelProperty(name = "payTime", value = "支付成功时间", dataType = "Date")
	private Date payTime;

	@TableField("third_trade_no")
	@ApiModelProperty(name = "thirdTradeNo", value = "三方单号", dataType = "String")
	private String thirdTradeNo;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
