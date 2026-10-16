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
import java.util.List;

/**
 * 剧单定义（独立于榜单 rank_board）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drama_playlist")
@ApiModel(value = "剧单", description = "剧场剧单 Tab 分块定义")
public class DramaPlaylistEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("group_id")
	@ApiModelProperty(name = "groupId", value = "剧单业务ID，跨语言共用", dataType = "String")
	private String groupId;

	@TableField("langue")
	@ApiModelProperty(name = "langue", value = "语言", dataType = "String")
	private String langue;

	@TableField("playlist_name")
	@ApiModelProperty(name = "playlistName", value = "展示名", dataType = "String")
	private String playlistName;

	@TableField("cover_url")
	@ApiModelProperty(name = "coverUrl", value = "剧单封面", dataType = "String")
	private String coverUrl;

	@TableField("match_mode")
	@ApiModelProperty(name = "matchMode", value = "1=OR 2=AND", dataType = "Integer")
	private Integer matchMode;

	@TableField("top_n")
	@ApiModelProperty(name = "topN", value = "预览条数", dataType = "Integer")
	private Integer topN;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "排序权重", dataType = "Integer")
	private Integer sortWeight;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用 0停用", dataType = "Integer")
	private Integer status;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;

	/** 管理端展示：绑定的剧标签 group_id 列表 */
	@TableField(exist = false)
	@ApiModelProperty(name = "tagGroupIds", value = "绑定标签 group_id 列表", dataType = "List")
	private List<String> tagGroupIds;
}
