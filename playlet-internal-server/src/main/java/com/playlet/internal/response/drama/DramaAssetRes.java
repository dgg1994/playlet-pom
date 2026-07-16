package com.playlet.internal.response.drama;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DramaAssetRes {

	@ApiModelProperty(name = "id",value = "主键",required = false,dataType = "Integer")
    private Integer id;

	@ApiModelProperty(name = "dramaId",value = "剧ID",required = true,dataType = "String")
	private Integer dramaId;
	
	@ApiModelProperty(name = "videoName",value = "视频名",required = true,dataType = "String")
	private String videoName;
	
	@ApiModelProperty(name = "setNum",value = "第几集",required = true,dataType = "Integer")
	private Integer setNum;
	
	@ApiModelProperty(name = "collectScore",value = "收藏量",required = false,dataType = "Long")
	private Long collectScore;
	
	@ApiModelProperty(name = "likeScore",value = "点赞量",required = false,dataType = "Long")
	private Long likeScore;
	
	@ApiModelProperty(name = "shareScore",value = "分享量",required = false,dataType = "Long")
	private Long shareScore;
	
	@ApiModelProperty(name = "discussScore",value = "评论量",required = false,dataType = "Long")
	private Long discussScore;
	
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@ApiModelProperty(name = "videoStatus",value = "可用状态 1是0否",required = false,dataType = "Integer")
	private Integer videoStatus;

	@ApiModelProperty(name = "deleteState",value = "删除状态 1是0否",required = false,dataType = "Integer")
	private Integer deleteState;

	@ApiModelProperty(name = "remarkInfo",value = "备注",required = false,dataType = "String")
	private String remarkInfo;

	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@ApiModelProperty(name = "setTime",value = "创建时间",required = false,dataType = "Date")
    private Date setTime;
	
	@ApiModelProperty(name = "gmtModified",value = "更新时间",required = false,dataType = "Date")
	private Date gmtModified;
	
}
