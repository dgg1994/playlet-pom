package com.playlet.internal.query.drama;
import java.util.List;

import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class QueryDramaQuery extends PageQueryHelperEntity{
	
	@ApiModelProperty(name = "dramaTitle",value = "短剧标题",required = true,dataType = "String")
	private String dramaTitle;
	
	@ApiModelProperty(name = "finishedState",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finishedState;
	
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@ApiModelProperty(name = "verifyStatus",value = "0草稿1待审2已上架3已下架",required = false,dataType = "Integer")
	private Integer verifyStatus;
	
	@ApiModelProperty(name = "isAi",value = "是否ai生成 1是0否",required = false,dataType = "Integer")
	private Integer isAi;
	
	@ApiModelProperty(name = "tagGroupIdList",value = "标签语言分组Id类型集合",required = false,dataType = "List<Integer>")
	private List<String> tagGroupIdList;
	
	@ApiModelProperty(name = "deleteState",value = "删除状态 1是0否",required = false,dataType = "Integer")
	private Integer deleteState;

}
