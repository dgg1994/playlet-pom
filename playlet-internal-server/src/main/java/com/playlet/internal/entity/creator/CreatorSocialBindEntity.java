package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * 创作者第三方账号绑定（微信 / 企微）。
 */
@Data
@TableName("creator_social_bind")
@ApiModel(value = "创作者第三方账号绑定")
public class CreatorSocialBindEntity {

	@TableId(type = IdType.AUTO)
	private Integer id;

	@TableField("creator_id")
	private Integer creatorId;

	@TableField("provider")
	private String provider;

	@TableField("open_id")
	private String openId;

	@TableField("union_id")
	private String unionId;

	@TableField("nickname")
	private String nickname;

	@TableField("avatar")
	private String avatar;

	@TableField("bind_time")
	private Date bindTime;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
