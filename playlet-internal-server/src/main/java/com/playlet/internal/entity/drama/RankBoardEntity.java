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
@TableName("rank_board")
@ApiModel(value = "榜单定义", description = "榜单定义")
public class RankBoardEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("board_code")
	@ApiModelProperty(name = "boardCode", value = "RECOMMEND/HOT/NEW", dataType = "String")
	private String boardCode;

	@TableField("board_name")
	@ApiModelProperty(name = "boardName", value = "展示名", dataType = "String")
	private String boardName;

	@TableField("board_type")
	@ApiModelProperty(name = "boardType", value = "1算法2人工", dataType = "Integer")
	private Integer boardType;

	@TableField("top_n")
	@ApiModelProperty(name = "topN", value = "最多名次", dataType = "Integer")
	private Integer topN;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1启用0停用", dataType = "Integer")
	private Integer status;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "首页分块排序", dataType = "Integer")
	private Integer sortWeight;

	@TableField("remark")
	@ApiModelProperty(name = "remark", value = "备注", dataType = "String")
	private String remark;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间", dataType = "Date")
	private Date gmtModified;
}
