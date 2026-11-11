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
 * 创作者收藏。
 */
@Data
@TableName("creator_favorite")
@ApiModel(value = "创作者收藏")
public class CreatorFavoriteEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "收藏人 creator_account.id")
	private Integer uid;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "被收藏创作者 creator_account.id")
	private Integer creatorId;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "收藏时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
