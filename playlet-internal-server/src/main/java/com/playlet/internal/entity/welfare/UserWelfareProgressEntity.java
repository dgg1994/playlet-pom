package com.playlet.internal.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_welfare_progress")
@ApiModel(value = "用户任务进度", description = "福利任务用户进度")
public class UserWelfareProgressEntity extends PageQueryHelperEntity {

	/** 进行中 */
	public static final int STATUS_DOING = 0;
	/** 可领取 */
	public static final int STATUS_CLAIMABLE = 1;
	/** 已领取 */
	public static final int STATUS_CLAIMED = 2;
	/** 已过期 */
	public static final int STATUS_EXPIRED = 3;
	/** 已放弃 */
	public static final int STATUS_ABANDONED = 4;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户uid", dataType = "String")
	private String uid;

	@TableField("task_id")
	@ApiModelProperty(name = "taskId", value = "任务id", dataType = "String")
	private String taskId;

	@TableField("biz_date")
	@ApiModelProperty(name = "bizDate", value = "周期键", dataType = "String")
	private String bizDate;

	@TableField("progress")
	@ApiModelProperty(name = "progress", value = "当前进度", dataType = "Integer")
	private Integer progress;

	@TableField("target")
	@ApiModelProperty(name = "target", value = "本周期目标快照", dataType = "Integer")
	private Integer target;

	@TableField("progress_status")
	@ApiModelProperty(name = "progressStatus", value = "0进行中 1可领取 2已领取 3已过期 4已放弃", dataType = "Integer")
	private Integer progressStatus;

	@TableField("claim_time")
	@ApiModelProperty(name = "claimTime", value = "领取时间", dataType = "Date")
	private Date claimTime;

	@TableField("expire_time")
	@ApiModelProperty(name = "expireTime", value = "过期时间", dataType = "Date")
	private Date expireTime;

	@TableField("ext_info")
	@ApiModelProperty(name = "extInfo", value = "JSON扩展", dataType = "String")
	private String extInfo;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
