package com.playlet.oversea.entity.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * 用户日活明细。
 */
@Data
@TableName("user_active_daily")
@ApiModel(value = "用户日活", description = "biz_date+uid 唯一")
public class UserActiveDailyEntity {

	@TableId(type = IdType.AUTO)
	private Long id;

	@TableField("biz_date")
	private String bizDate;

	@TableField("uid")
	private Integer uid;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
