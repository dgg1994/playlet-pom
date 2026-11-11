package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 征稿参与记录。
 */
@Data
@TableName("creator_call_join")
@ApiModel(value = "征稿参与记录")
public class CreatorCallJoinEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("call_id")
	@ApiModelProperty(name = "callId", value = "creator_call.id")
	private Integer callId;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "参与人 creator_account.id")
	private Integer creatorId;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "参与时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
