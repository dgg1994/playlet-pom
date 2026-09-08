package com.playlet.oversea.entity.welfare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("user_sign_in")
@ApiModel(value = "用户签到状态", description = "每用户一行摘要")
public class UserSignInEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户uid", dataType = "String")
	private String uid;

	@TableField("streak_days")
	@ApiModelProperty(name = "streakDays", value = "当前连续天数", dataType = "Integer")
	private Integer streakDays;

	@TableField("last_sign_date")
	@ApiModelProperty(name = "lastSignDate", value = "最近签到日 yyyy-MM-dd", dataType = "String")
	private String lastSignDate;

	@TableField("total_sign_days")
	@ApiModelProperty(name = "totalSignDays", value = "累计签到天数（含补签）", dataType = "Integer")
	private Integer totalSignDays;

	@TableField("makeup_card_balance")
	@ApiModelProperty(name = "makeupCardBalance", value = "补签卡余额", dataType = "Integer")
	private Integer makeupCardBalance;

	@TableField("makeup_buy_month")
	@ApiModelProperty(name = "makeupBuyMonth", value = "补签卡购买统计月份 yyyy-MM", dataType = "String")
	private String makeupBuyMonth;

	@TableField("makeup_buy_count")
	@ApiModelProperty(name = "makeupBuyCount", value = "该月已购补签卡张数", dataType = "Integer")
	private Integer makeupBuyCount;

	/** 新手期起始业务日 yyyy-MM-dd */
	@TableField("newbie_start_date")
	@ApiModelProperty(name = "newbieStartDate", value = "新手期起始日 yyyy-MM-dd", dataType = "String")
	private String newbieStartDate;

	/** 新手期内已签次数 */
	@TableField("newbie_sign_count")
	@ApiModelProperty(name = "newbieSignCount", value = "新手期内已签次数", dataType = "Integer")
	private Integer newbieSignCount;

	/** 新手是否已结束：1是 0否 */
	@TableField("newbie_finished")
	@ApiModelProperty(name = "newbieFinished", value = "新手是否已结束 1是 0否", dataType = "Integer")
	private Integer newbieFinished;

	/** 结束原因：1=FULL 2=EXPIRED */
	@TableField("newbie_end_reason")
	@ApiModelProperty(name = "newbieEndReason", value = "结束原因 1满次数 2窗口过期", dataType = "Integer")
	private Integer newbieEndReason;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
