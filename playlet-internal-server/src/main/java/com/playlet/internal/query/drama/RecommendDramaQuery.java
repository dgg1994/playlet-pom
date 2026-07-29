package com.playlet.internal.query.drama;

import com.playlet.internal.query.pub.PageQueryHelperEntity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RecommendDramaQuery extends PageQueryHelperEntity{
	
	@ApiModelProperty(name = "verifyStatus",value = "0下架1上架",required = false,dataType = "Integer")
	private Integer verifyStatus;

	@ApiModelProperty(name = "deleteState",value = "1是0否",required = false,dataType = "Integer")
	private Integer deleteState;

	@ApiModelProperty(name = "seed", value = "推荐会话种子；首页可空由服务端生成，翻页必须回传", required = false, dataType = "String")
	private String seed;

}
