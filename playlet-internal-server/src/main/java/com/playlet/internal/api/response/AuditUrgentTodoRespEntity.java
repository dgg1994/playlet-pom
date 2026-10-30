package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 审核汇总：今日紧急待办条目
 */
@Data
@ApiModel("审核汇总-紧急待办")
public class AuditUrgentTodoRespEntity {

	@ApiModelProperty("模块编码：DRAMA_INFO/DRAMA_EPISODE/DRAMA_RECORD/COMMENT/SENSITIVE_WORD")
	private String module;

	@ApiModelProperty("左侧标签文案，如 一类复审 / 超时")
	private String tagLabel;

	@ApiModelProperty("标签样式：danger=超时或高危，warning=复审，info=初审/待审")
	private String tagType;

	@ApiModelProperty("待办标题，如 总裁的隐婚甜妻 - 备案复审")
	private String itemTitle;

	@ApiModelProperty("剩余处理分钟数；负数表示已超时多少分钟")
	private Integer remainMinutes;

	@ApiModelProperty("是否已超时")
	private Boolean overdue;

	@ApiModelProperty("立即处理跳转路径")
	private String targetPath;
}
