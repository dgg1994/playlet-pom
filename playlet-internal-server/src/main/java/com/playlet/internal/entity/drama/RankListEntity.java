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
@TableName("rank_list")
@ApiModel(value = "榜单条目", description = "榜单条目（展示字段联查 drama，本表仅存关系与状态）")
public class RankListEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("board_group_id")
	@ApiModelProperty(name = "boardGroupId", value = "所属榜id", dataType = "String")
	private String boardGroupId;

	@TableField("rank_no")
	@ApiModelProperty(name = "rankNo", value = "名次从1开始", dataType = "Integer")
	private Integer rankNo;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "业务剧ID", dataType = "String")
	private String dramaId;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1生效0停用", dataType = "Integer")
	private Integer status;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;

}
