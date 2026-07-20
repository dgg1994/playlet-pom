package com.playlet.internal.entity.account;

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

/**
 * 用户关注关系：uid 关注 followUid。
 * 我的关注 = uid=当前用户；我的粉丝 = followUid=当前用户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_follow")
@ApiModel(value = "用户关注关系", description = "关注/粉丝绑定")
public class UserFollowEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "关注人uid（粉丝）", dataType = "String")
	private Integer uid;

	@TableField("follow_uid")
	@ApiModelProperty(name = "followUid", value = "被关注人uid", dataType = "String")
	private Integer followUid;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "关注时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
