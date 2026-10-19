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
 * 用户剧集解锁记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_episode_unlock")
@ApiModel(value = "剧集解锁", description = "用户已解锁的剧集")
public class UserEpisodeUnlockEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Integer")
	private Integer uid;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "剧ID", dataType = "Integer")
	private Integer dramaId;

	@TableField("asset_id")
	@ApiModelProperty(name = "assetId", value = "集ID", dataType = "Integer")
	private Integer assetId;

	@TableField("unlock_way")
	@ApiModelProperty(name = "unlockWay", value = "FREE/AD/COIN/CASH/MEMBER", dataType = "String")
	private String unlockWay;

	@TableField("cost_coin")
	@ApiModelProperty(name = "costCoin", value = "消耗金币", dataType = "Integer")
	private Integer costCoin;

	@TableField("cost_yuan")
	@ApiModelProperty(name = "costYuan", value = "消耗现金(元)", dataType = "BigDecimal")
	private BigDecimal costYuan;

	@TableField("biz_no")
	@ApiModelProperty(name = "bizNo", value = "订单号或广告transId", dataType = "String")
	private String bizNo;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "解锁时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
