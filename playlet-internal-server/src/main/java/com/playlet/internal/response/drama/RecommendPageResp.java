package com.playlet.internal.response.drama;

import com.github.pagehelper.PageInfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("推荐分页结果")
public class RecommendPageResp {

	@ApiModelProperty("会话随机种子，翻页时原样回传以保证不重复")
	private String seed;

	@ApiModelProperty("分页结果")
	private PageInfo<RecommendDramaRes> page;
}
