package com.playlet.oversea.api.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 删除浏览历史入参（支持批量）。
 */
@Data
@ApiModel("删除浏览历史")
public class WatchHistoryDeleteReqEntity {

	@NotEmpty(message = "短剧ID集合不能为空")
	@ApiModelProperty(value = "要删除的短剧业务ID集合", required = true, example = "[11,12,13]")
	private List<Integer> dramaIds;
}
