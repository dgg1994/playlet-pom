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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rank_list")
@ApiModel(value = "榜单条目", description = "榜单条目")
public class RankListEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键", dataType = "Integer")
	private Integer id;

	@TableField("board_code")
	@ApiModelProperty(name = "boardCode", value = "所属榜", dataType = "String")
	private String boardCode;

	@TableField("rank_no")
	@ApiModelProperty(name = "rankNo", value = "名次从1开始", dataType = "Integer")
	private Integer rankNo;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "业务剧ID", dataType = "String")
	private String dramaId;

	@TableField("score")
	@ApiModelProperty(name = "score", value = "分数", dataType = "BigDecimal")
	private BigDecimal score;

	@TableField("title")
	@ApiModelProperty(name = "title", value = "标题冗余", dataType = "String")
	private String title;

	@TableField("cover_url")
	@ApiModelProperty(name = "coverUrl", value = "封面冗余", dataType = "String")
	private String coverUrl;

	@TableField("hot_score_text")
	@ApiModelProperty(name = "hotScoreText", value = "热度文案", dataType = "String")
	private String hotScoreText;

	@TableField("total_episodes")
	@ApiModelProperty(name = "totalEpisodes", value = "总集数", dataType = "Integer")
	private Integer totalEpisodes;

	@TableField("finished")
	@ApiModelProperty(name = "finished", value = "1完结0连载", dataType = "Integer")
	private Integer finished;

	@TableField("status")
	@ApiModelProperty(name = "status", value = "1生效0停用", dataType = "Integer")
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

	@TableField(exist = false)
	@ApiModelProperty(name = "items", value = "整榜覆盖写入用", dataType = "List")
	private List<RankListEntity> items;
}
