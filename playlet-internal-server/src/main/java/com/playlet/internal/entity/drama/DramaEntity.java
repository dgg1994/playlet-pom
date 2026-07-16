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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drama")
@ApiModel(value = "短剧",description = "短剧")
public class DramaEntity extends PageQueryHelperEntity {
	
	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@TableField("drama_title")
	@ApiModelProperty(name = "dramaTitle",value = "短剧标题",required = true,dataType = "String")
	private String dramaTitle;
	
	@TableField("producer_firm")
	@ApiModelProperty(name = "producerFirm",value = "出品方",required = true,dataType = "String")
	private String producerFirm;
	
	@TableField("cover_url")
	@ApiModelProperty(name = "coverUrl",value = "默认封面URL",required = false,dataType = "String")
	private String coverUrl;
	
	@TableField("score_num")
	@ApiModelProperty(name = "scoreNum",value = "评分",required = false,dataType = "Long")
	private Long scoreNum;
	
	@TableField("hot_score")
	@ApiModelProperty(name = "hotScore",value = "热度值",required = false,dataType = "Long")
	private Long hotScore;
	
	@TableField("collect_score")
	@ApiModelProperty(name = "collectScore",value = "收藏量",required = false,dataType = "Long")
	private Long collectScore;
	
	@TableField("like_score")
	@ApiModelProperty(name = "likeScore",value = "点赞量",required = false,dataType = "Long")
	private Long likeScore;
	
	@TableField("share_score")
	@ApiModelProperty(name = "shareScore",value = "分享量",required = false,dataType = "Long")
	private Long shareScore;
	
	@TableField("discuss_score")
	@ApiModelProperty(name = "discussScore",value = "评论量",required = false,dataType = "Long")
	private Long discussScore;
	
	@TableField("hot_score_text")
	@ApiModelProperty(name = "hotScoreText",value = "热度文案",required = false,dataType = "String")
	private String hotScoreText;
	
	@TableField("total_episodes")
	@ApiModelProperty(name = "totalEpisodes",value = "总集数",required = false,dataType = "Integer")
	private Integer totalEpisodes;
	
	@TableField("finished_state")
	@ApiModelProperty(name = "finishedState",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finishedState;
	
	@TableField("video_width")
	@ApiModelProperty(name = "videoWidth",value = "宽",required = false,dataType = "Integer")
	private Integer videoWidth;
	
	@TableField("video_height")
	@ApiModelProperty(name = "videoHeight",value = "高",required = false,dataType = "Integer")
	private Integer videoHeight;
	
	@TableField("description_info")
	@ApiModelProperty(name = "descriptionInfo",value = "简介",required = false,dataType = "String")
	private String descriptionInfo;
	
	@TableField("verify_status")
	@ApiModelProperty(name = "verifyStatus",value = "0草稿1待审2已上架3已下架",required = false,dataType = "Integer")
	private Integer verifyStatus;

	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState",value = "1是0否",required = false,dataType = "Integer")
	private Integer deleteState;

	@TableField("offline_reason")
	@ApiModelProperty(name = "offlineReason",value = "下架原因",required = false,dataType = "String")
	private String offlineReason;

	@TableField("remark_info")
	@ApiModelProperty(name = "remarkInfo",value = "运营备注",required = false,dataType = "String")
	private String remarkInfo;

	@TableField("belong_user")
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@TableField("setTime")
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
	@TableField(exist = false)
	@ApiModelProperty(name = "tagList",value = "标签类型集合",required = false,dataType = "List<Integer>")
	private List<TagEntity> tagList;


}
