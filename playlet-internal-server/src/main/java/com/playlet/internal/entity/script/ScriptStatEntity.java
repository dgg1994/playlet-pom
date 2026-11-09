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
 * 剧本市场-剧本计数。
 */
@Data
@TableName("script_stat")
@ApiModel(value = "剧本计数", description = "浏览/收藏/申请/成交计数")
public class ScriptStatEntity {

	@TableId(value = "script_id", type = IdType.INPUT)
	@ApiModelProperty(name = "scriptId", value = "script.id")
	private Integer scriptId;

	@TableField("view_count")
	@ApiModelProperty(name = "viewCount", value = "浏览次数")
	private Long viewCount;

	@TableField("favorite_count")
	@ApiModelProperty(name = "favoriteCount", value = "收藏次数")
	private Long favoriteCount;

	@TableField("apply_count")
	@ApiModelProperty(name = "applyCount", value = "申请次数")
	private Long applyCount;

	@TableField("deal_count")
	@ApiModelProperty(name = "dealCount", value = "成交次数")
	private Long dealCount;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
