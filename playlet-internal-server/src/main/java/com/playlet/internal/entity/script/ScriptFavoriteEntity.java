package com.playlet.internal.entity.script;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 剧本市场-用户收藏。
 */
@Data
@TableName("script_favorite")
@ApiModel(value = "剧本收藏", description = "用户收藏关系")
public class ScriptFavoriteEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("uid")
	@ApiModelProperty(name = "uid", value = "app_account.id")
	private Integer uid;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "收藏时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
