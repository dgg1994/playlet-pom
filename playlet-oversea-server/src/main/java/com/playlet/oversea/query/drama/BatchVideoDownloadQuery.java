package com.playlet.oversea.query.drama;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@ApiModel("批量视频下载请求")
public class BatchVideoDownloadQuery {

	@NotEmpty(message = "资源id列表不能为空")
	@Size(max = 50, message = "单次最多50个资源")
	@ApiModelProperty(name = "ids", value = "drama_asset.id 列表", required = true, dataType = "List")
	private List<Integer> ids;

	@NotBlank(message = "分辨率不能为空")
	@ApiModelProperty(name = "definition", value = "清晰度 360/480/720/1080", required = true, dataType = "String")
	private String definition;
}
