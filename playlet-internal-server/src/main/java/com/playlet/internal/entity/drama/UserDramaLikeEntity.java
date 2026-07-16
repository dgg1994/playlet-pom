package com.playlet.internal.entity.drama;

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
@TableName("user_drama_like")
@ApiModel(value = "用户点赞", description = "用户点赞（整剧/单集）")
public class UserDramaLikeEntity extends PageQueryHelperEntity {

	/** 整剧点赞 */
	public static final int LIKE_TYPE_DRAMA = 1;
	/** 单集点赞 */
	public static final int LIKE_TYPE_EPISODE = 2;

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Long")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "用户ID", dataType = "String")
	private String uid;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "短剧主键 drama.id", dataType = "Integer")
	private Integer dramaId;

	@TableField("like_type")
	@ApiModelProperty(name = "likeType", value = "1整剧点赞 2单集点赞", dataType = "Integer")
	private Integer likeType;

	@TableField("episode_id")
	@ApiModelProperty(name = "episodeId", value = "单集ID；整剧点赞时为空串", dataType = "String")
	private String episodeId;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "点赞时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
