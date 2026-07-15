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
@TableName("drama")
@ApiModel(value = "短剧",description = "短剧")
public class DramaEntity extends PageQueryHelperEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId",value = "业务剧ID",required = false,dataType = "String")
	private String dramaId;
	
	@TableField("title")
	@ApiModelProperty(name = "title",value = "标题",required = true,dataType = "String")
	private String title;
	
	@TableField("cover_url")
	@ApiModelProperty(name = "coverUrl",value = "默认封面URL",required = false,dataType = "String")
	private String coverUrl;
	
	@TableField("hot_score")
	@ApiModelProperty(name = "hotScore",value = "热度值",required = false,dataType = "Long")
	private Long hotScore;
	
	@TableField("hot_score_text")
	@ApiModelProperty(name = "hotScoreText",value = "热度文案",required = false,dataType = "String")
	private String hotScoreText;
	
	@TableField("total_episodes")
	@ApiModelProperty(name = "totalEpisodes",value = "总集数",required = false,dataType = "Integer")
	private Integer totalEpisodes;
	
	@TableField("finished")
	@ApiModelProperty(name = "finished",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finished;
	
	@TableField("description")
	@ApiModelProperty(name = "description",value = "简介",required = false,dataType = "String")
	private String description;
	
	@TableField("status")
	@ApiModelProperty(name = "status",value = "0草稿1待审2已上架3已下架",required = false,dataType = "Integer")
	private Integer status;

	@TableField("deleted")
	@ApiModelProperty(name = "deleted",value = "0正常1软删",required = false,dataType = "Integer")
	private Integer deleted;

	@TableField("offline_reason")
	@ApiModelProperty(name = "offlineReason",value = "下架原因",required = false,dataType = "String")
	private String offlineReason;

	@TableField("remark")
	@ApiModelProperty(name = "remark",value = "运营备注",required = false,dataType = "String")
	private String remark;

	@TableField("create_by")
	@ApiModelProperty(name = "createBy",value = "创建人",required = false,dataType = "String")
	private String createBy;

	@TableField("update_by")
	@ApiModelProperty(name = "updateBy",value = "更新人",required = false,dataType = "String")
	private String updateBy;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;

	@TableField(exist = false)
	@ApiModelProperty(name = "orientation",value = "方向（1竖2横）查询用",required = false,dataType = "Integer")
	private Integer orientation;

	@TableField(exist = false)
	@ApiModelProperty(name = "episodeId",value = "推荐起播分集ID",required = false,dataType = "String")
	private String episodeId;

	@TableField(exist = false)
	@ApiModelProperty(name = "episodeNo",value = "推荐起播集序号",required = false,dataType = "Integer")
	private Integer episodeNo;

	@TableField(exist = false)
	@ApiModelProperty(name = "playUrl",value = "默认播放地址",required = false,dataType = "String")
	private String playUrl;

	@TableField(exist = false)
	@ApiModelProperty(name = "statusName",value = "状态名称",required = false,dataType = "String")
	private String statusName;

}
