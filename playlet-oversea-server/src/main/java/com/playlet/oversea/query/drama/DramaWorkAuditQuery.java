package com.playlet.oversea.query.drama;

import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作品评审列表查询（剧/集）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DramaWorkAuditQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "workType", value = "作品类型：1剧 2集，空=全部", required = false, dataType = "Integer")
	private Integer workType;

	@ApiModelProperty(name = "keyword", value = "搜索关键词（剧名/集名）", required = false, dataType = "String")
	private String keyword;

	@ApiModelProperty(name = "dramaTitle", value = "剧名模糊检索（集列表）", required = false, dataType = "String")
	private String dramaTitle;

	@ApiModelProperty(name = "dramaId", value = "按剧过滤（集列表用）", required = false, dataType = "Integer")
	private Integer dramaId;

	@ApiModelProperty(name = "auditStatus", value = "审核状态：0/1待审核（含待审+审核中） 2通过 3驳回 4申诉中", required = false, dataType = "Integer")
	private Integer auditStatus;

	@ApiModelProperty(name = "langue", value = "标签语言，如 zh-cn；空则取请求头 x-playlet-language", required = false, dataType = "String")
	private String langue;
}
