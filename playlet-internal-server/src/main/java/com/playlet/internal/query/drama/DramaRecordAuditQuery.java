package com.playlet.internal.query.drama;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 备案审核列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DramaRecordAuditQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "keyword", value = "搜索关键词（剧名/出品方）", dataType = "String")
	private String keyword;

	@ApiModelProperty(name = "recordStatus", value = "备案状态：0待初审 1初审通过待复审 2备案通过 3初审驳回 4复审驳回 5已上架；空=全部", dataType = "Integer")
	private Integer recordStatus;

	@ApiModelProperty(name = "filingCategory", value = "备案分类：1一类 2二类 3三类；空=全部", dataType = "Integer")
	private Integer filingCategory;
}
