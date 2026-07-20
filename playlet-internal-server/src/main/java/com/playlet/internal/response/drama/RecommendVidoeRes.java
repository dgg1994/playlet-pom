package com.playlet.internal.response.drama;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RecommendVidoeRes {
	
	@ApiModelProperty(name = "id",value = "视频id",required = true,dataType = "String")
	private Integer id;

	@ApiModelProperty(name = "videoName",value = "视频名",required = true,dataType = "String")
	private String videoName;
	
	@ApiModelProperty(name = "setNum",value = "第几集",required = true,dataType = "Integer")
	private Integer setNum;
	
	@ApiModelProperty(name = "collectScore",value = "收藏量",required = false,dataType = "Long")
	private Long collectScore;
	
	@ApiModelProperty(name = "shareScore",value = "分享量",required = false,dataType = "Long")
	private Long shareScore;
	
	@ApiModelProperty(name = "likeScore",value = "点赞量",required = false,dataType = "Long")
	private Long likeScore;
	
	@ApiModelProperty(name = "discussScore",value = "评论量",required = false,dataType = "Long")
	private Long discussScore;
	
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@ApiModelProperty(name = "videoUrl",value = "视频资源URL",required = true,dataType = "String")
	private String videoUrl;
	
	@ApiModelProperty(name = "isLike",value = "是否点赞 1是0否",required = true,dataType = "String")
	private Integer isLike;
	
	@ApiModelProperty(name = "isCollect",value = "是否收藏 1是0否",required = true,dataType = "String")
	private Integer isCollect;
}