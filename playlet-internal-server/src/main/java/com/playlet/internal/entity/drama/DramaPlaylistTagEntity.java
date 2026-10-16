package com.playlet.internal.entity.drama;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 剧单绑定的剧标签。
 */
@Data
@TableName("drama_playlist_tag")
@ApiModel(value = "剧单标签", description = "剧单与 dic_drama_tag 多对多")
public class DramaPlaylistTagEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("playlist_group_id")
	@ApiModelProperty(name = "playlistGroupId", value = "剧单 group_id", dataType = "String")
	private String playlistGroupId;

	@TableField("tag_group_id")
	@ApiModelProperty(name = "tagGroupId", value = "剧标签 group_id", dataType = "String")
	private String tagGroupId;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "标签顺序", dataType = "Integer")
	private Integer sortWeight;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
