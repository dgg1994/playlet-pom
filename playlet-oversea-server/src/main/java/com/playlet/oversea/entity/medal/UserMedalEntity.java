package com.playlet.oversea.entity.medal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_medal")
@ApiModel(value = "用户勋章进度", description = "每用户每勋章一条记录")
public class UserMedalEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "Long")
	private Long uid;

	@TableField("medal_id")
	@ApiModelProperty(name = "medalId", value = "勋章id", dataType = "Integer")
	private Integer medalId;

	@TableField("progress")
	@ApiModelProperty(name = "progress", value = "当前进度值", dataType = "Integer")
	private Integer progress;

	@TableField("target_count")
	@ApiModelProperty(name = "targetCount", value = "目标次数快照", dataType = "Integer")
	private Integer targetCount;

	@TableField("unlocked")
	@ApiModelProperty(name = "unlocked", value = "是否解锁：0未解锁 1已解锁", dataType = "Integer")
	private Integer unlocked;

	@TableField("unlock_time")
	@ApiModelProperty(name = "unlockTime", value = "解锁时间", dataType = "Date")
	private Date unlockTime;

	@TableField("notify_status")
	@ApiModelProperty(name = "notifyStatus", value = "解锁弹窗提醒：0未提醒 1已提醒", dataType = "Integer")
	private Integer notifyStatus;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
