package com.playlet.internal.response.drama;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RecommendDramaRes {
	
	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@ApiModelProperty(name = "dramaTitle",value = "短剧标题",required = true,dataType = "String")
	private String dramaTitle;
	
	@ApiModelProperty(name = "producerFirm",value = "出品方",required = true,dataType = "String")
	private String producerFirm;
	
	@TableField("score_num")
	@ApiModelProperty(name = "scoreNum",value = "评分",required = false,dataType = "Long")
	private Long scoreNum;
	
	@ApiModelProperty(name = "hotScore",value = "热度值(播放量)",required = false,dataType = "Long")
	private Long hotScore;
	
	@ApiModelProperty(name = "collectScore",value = "收藏量",required = false,dataType = "Long")
	private Long collectScore;
	
	@ApiModelProperty(name = "shareScore",value = "分享量",required = false,dataType = "Long")
	private Long shareScore;
	
	@ApiModelProperty(name = "totalEpisodes",value = "总集数",required = false,dataType = "Integer")
	private Integer totalEpisodes;
	
	@ApiModelProperty(name = "finishedState",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finishedState;
	
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@ApiModelProperty(name = "descriptionInfo",value = "简介",required = false,dataType = "String")
	private String descriptionInfo;
	
	@ApiModelProperty(name = "coverUrl",value = "封面",required = false,dataType = "String")
	private String coverUrl;
	
	@ApiModelProperty(name = "isAi",value = "是否ai生成 1是0否",required = false,dataType = "Integer")
	private Integer isAi;
	
	@ApiModelProperty(name = "vidoeRes",value = "视频内容",required = false,dataType = "RecommendVidoeRes")
	private RecommendVidoeRes vidoeRes;

}
