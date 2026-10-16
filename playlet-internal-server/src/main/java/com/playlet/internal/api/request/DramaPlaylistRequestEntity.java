package com.playlet.internal.api.request;

import com.playlet.internal.entity.drama.DramaPlaylistEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 剧单新增/编辑请求（多语言行 + 共享标签与策略）。
 */
@Data
@ApiModel(value = "剧单保存请求", description = "管理端剧单新增/编辑入参")
public class DramaPlaylistRequestEntity {

	@ApiModelProperty("编辑时必填：剧单业务ID")
	private String groupId;

	@ApiModelProperty("各语言展示行（至少一行）")
	private List<DramaPlaylistEntity> playlists;

	@ApiModelProperty("绑定的剧标签 group_id 列表")
	private List<String> tagGroupIds;

	@ApiModelProperty("1=OR 2=AND，默认1")
	private Integer matchMode;

	@ApiModelProperty("首页预览条数，默认10")
	private Integer topN;

	@ApiModelProperty("排序权重，越大越靠前")
	private Integer sortWeight;

	@ApiModelProperty("1启用 0停用")
	private Integer status;

	@ApiModelProperty("剧单封面")
	private String coverUrl;

	@ApiModelProperty("运营备注")
	private String remark;
}
