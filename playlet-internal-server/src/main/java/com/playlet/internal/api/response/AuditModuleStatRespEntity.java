package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核汇总：单个模块的统计卡片数据
 */
@Data
@ApiModel("审核汇总-模块统计")
public class AuditModuleStatRespEntity {

	@ApiModelProperty("模块编码：DRAMA_INFO/DRAMA_EPISODE/DRAMA_RECORD/COMMENT/SENSITIVE_WORD/CREATOR_ENTRY")
	private String module;

	@ApiModelProperty("模块名称")
	private String moduleName;

	@ApiModelProperty("待审核数")
	private Integer pendingCount;

	@ApiModelProperty("通过数（敏感词模块为已生效词数）")
	private Integer passedCount;

	@ApiModelProperty("驳回数")
	private Integer rejectedCount;

	@ApiModelProperty("累计总量")
	private Integer totalCount;

	@ApiModelProperty("今日完成数（敏感词模块为今日命中数）")
	private Integer todayCount;

	@ApiModelProperty("超时未处理数")
	private Integer overdueCount;

	@ApiModelProperty("平均处理时长（小时）")
	private Double avgHours;

	@ApiModelProperty("通过率（0~100，保留1位小数）")
	private Double passRate;

	@ApiModelProperty("展示模式：AUDIT=待审/通过率；WORD=词库总量/今日命中；ENTRY=待审/通过率")
	private String statMode;

	@ApiModelProperty("前往处理路径；空表示暂未提供管理页")
	private String targetPath;
}
