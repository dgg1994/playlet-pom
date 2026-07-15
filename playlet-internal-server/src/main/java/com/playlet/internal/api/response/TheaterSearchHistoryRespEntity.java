package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("剧场搜索历史")
public class TheaterSearchHistoryRespEntity {

	@ApiModelProperty("历史关键词，最近的在前")
	private List<String> keywords = new ArrayList<>();
}
