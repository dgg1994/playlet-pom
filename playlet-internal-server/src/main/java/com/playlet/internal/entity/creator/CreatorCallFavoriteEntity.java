package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 创作者征稿收藏。
 */
@Data
@TableName("creator_call_favorite")
public class CreatorCallFavoriteEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/** 收藏人 creator_account.id */
	@TableField("creator_id")
	private Integer creatorId;

	/** 征稿 creator_call.id */
	@TableField("call_id")
	private Integer callId;

	@TableField("setTime")
	private Date setTime;

	@TableField("gmtModified")
	private Date gmtModified;
}
