package com.playlet.internal.api.response;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.entity.drama.DramaEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * C 端剧单二级页分页结果。
 */
@Data
@ApiModel("剧场剧单分页")
public class TheaterPlaylistPageRespEntity {

	@ApiModelProperty("剧单 groupId")
	private String groupId;

	@ApiModelProperty("剧单展示名")
	private String playlistName;

	@ApiModelProperty("1=OR 2=AND")
	private Integer matchMode;

	@ApiModelProperty("剧单封面")
	private String coverUrl;

	@ApiModelProperty("分页结果")
	private PageInfo<DramaEntity> page;
}
