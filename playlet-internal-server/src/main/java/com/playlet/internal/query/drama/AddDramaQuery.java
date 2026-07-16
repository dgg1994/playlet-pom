package com.playlet.internal.query.drama;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddDramaQuery {
	
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
	
	@NotNull(message = "分辨率不能为空")
	@ApiModelProperty(name = "videoWidth",value = "宽",required = false,dataType = "Integer")
	private Integer videoWidth;
	
	@NotNull(message = "分辨率不能为空")
	@ApiModelProperty(name = "videoHeight",value = "高",required = false,dataType = "Integer")
	private Integer videoHeight;
	
	@NotBlank(message = "简介不能为空")
	@ApiModelProperty(name = "descriptionInfo",value = "简介",required = false,dataType = "String")
	private String descriptionInfo;
	
	@NotNull(message = "所属人不能为空")
	@ApiModelProperty(name = "belongUser",value = "所属人",required = false,dataType = "String")
	private Integer belongUser;
	
	@NotNull(message = "标签不能为空")
	@ApiModelProperty(name = "tagGroupIdList",value = "标签语言分组Id类型集合",required = false,dataType = "List<Integer>")
	private List<String> tagGroupIdList;

}
