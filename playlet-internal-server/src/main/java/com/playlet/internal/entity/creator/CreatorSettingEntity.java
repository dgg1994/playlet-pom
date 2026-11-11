package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * 创作者隐私与推送设置。
 */
@Data
@TableName("creator_setting")
@ApiModel(value = "创作者隐私与推送设置")
public class CreatorSettingEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("creator_id")
	private Integer creatorId;

	/** 主页可见 1公开 0仅自己 */
	@TableField("profile_visible")
	private Integer profileVisible;

	/** 联系方式对合作方可见 1是 0否 */
	@TableField("contact_visible")
	private Integer contactVisible;

	/** 作品公开 1是 0否 */
	@TableField("work_visible")
	private Integer workVisible;

	/** 系统通知推送 1开 0关 */
	@TableField("push_system")
	private Integer pushSystem;

	/** 合作消息推送 1开 0关 */
	@TableField("push_coop")
	private Integer pushCoop;

	/** 活动推送 1开 0关 */
	@TableField("push_activity")
	private Integer pushActivity;

	/** 征稿投稿消息推送 1开 0关 */
	@TableField("push_submit")
	private Integer pushSubmit;

	/** 合同消息推送 1开 0关 */
	@TableField("push_contract")
	private Integer pushContract;

	/** 审核消息推送 1开 0关 */
	@TableField("push_audit")
	private Integer pushAudit;

	/** 收益结算推送 1开 0关 */
	@TableField("push_income")
	private Integer pushIncome;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
