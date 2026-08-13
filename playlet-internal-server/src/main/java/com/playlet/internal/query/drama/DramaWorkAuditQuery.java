package com.playlet.internal.query.drama;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DramaWorkAuditQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "listTab", value = "列表页签：1待审核 2申诉 3完审 4驳回", required = false, dataType = "Integer")
	private Integer listTab;

	@ApiModelProperty(name = "workType", value = "作品类型：1剧 2集，空=全部", required = false, dataType = "Integer")
	private Integer workType;

	@ApiModelProperty(name = "keyword", value = "搜索关键词（剧名/集名）", required = false, dataType = "String")
	private String keyword;

	@ApiModelProperty(name = "dramaTitle", value = "剧名模糊检索（集列表）", required = false, dataType = "String")
	private String dramaTitle;

	@ApiModelProperty(name = "dramaId", value = "按剧过滤（集列表用）", required = false, dataType = "Integer")
	private Integer dramaId;

	@ApiModelProperty(name = "auditStatus", value = "审核状态：0待审 1审核中 2通过 3驳回", required = false, dataType = "Integer")
	private Integer auditStatus;
}
