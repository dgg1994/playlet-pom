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
 * 剧本市场-人物介绍。
 */
@Data
@TableName("script_character")
@ApiModel(value = "剧本人物", description = "人物介绍")
public class ScriptCharacterEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Long id;

	@TableField("script_id")
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("char_name")
	@ApiModelProperty(name = "charName", value = "人物姓名")
	private String charName;

	@TableField("char_role")
	@ApiModelProperty(name = "charRole", value = "角色 1男主 2女主 3男二 4女二 9其他")
	private Integer charRole;

	@TableField("avatar_url")
	@ApiModelProperty(name = "avatarUrl", value = "人物头像七牛 key")
	private String avatarUrl;

	@TableField("description")
	@ApiModelProperty(name = "description", value = "人物介绍")
	private String description;

	@TableField("sort_order")
	@ApiModelProperty(name = "sortOrder", value = "排序")
	private Integer sortOrder;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
