package com.playlet.internal.query.drama;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UpdateDramaQuery {
	
	@NotNull(message = "id不能为空")
	@ApiModelProperty(name = "id",value = "短剧主键id",required = true,dataType = "String")
	private Integer id;
	
	@NotBlank(message = "短剧标题不能为空")
	@ApiModelProperty(name = "dramaTitle",value = "短剧标题",required = true,dataType = "String")
	private String dramaTitle;
	
	@ApiModelProperty(name = "producerFirm",value = "出品方",required = true,dataType = "String")
	private String producerFirm;
	
	@NotNull(message = "总集数不能为空")
	@ApiModelProperty(name = "totalEpisodes",value = "总集数",required = false,dataType = "Integer")
	private Integer totalEpisodes;
	
	@NotNull(message = "是否完结不能为空")
	@ApiModelProperty(name = "finishedState",value = "是否完结（1是0否）",required = false,dataType = "Integer")
	private Integer finishedState;
	
//	@NotNull(message = "分辨率不能为空")
//	@ApiModelProperty(name = "videoWidth",value = "宽",required = false,dataType = "Integer")
//	private Integer videoWidth;
//	
//	@NotNull(message = "分辨率不能为空")
//	@ApiModelProperty(name = "videoHeight",value = "高",required = false,dataType = "Integer")
//	private Integer videoHeight;
	
	@NotNull(message = "视频类型不能为空")
	@ApiModelProperty(name = "videoType",value = "视频类型 1横屏 2竖屏",required = false,dataType = "Integer")
	private Integer videoType;
	
	@NotBlank(message = "简介不能为空")
	@ApiModelProperty(name = "descriptionInfo",value = "简介",required = false,dataType = "String")
	private String descriptionInfo;
	
	@NotNull(message = "所属人不能为空")
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@ApiModelProperty(name = "tagGroupIdList",value = "标签语言分组Id类型集合",required = false,dataType = "List<Integer>")
	private List<String> tagGroupIdList;

}
