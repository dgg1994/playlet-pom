package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorCertificationSaveQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证管理（作家端）：个人/机构双类别，草稿 + 提交审核 + 审核记录。
 */
@RequestMapping("/api/creator/certification")
@Api(value = "作家端认证管理", tags = "作家端认证管理")
public interface CreatorCertificationService {

	/** 表单字典选项（认证身份 / 擅长类型 / 市场 / 领域 / 承接服务） */
	@GetMapping("/options")
	@ApiOperation("认证表单字典选项")
	ResponseBase options(HttpServletRequest request);

	/** 认证详情（草稿回显 + 审核状态） */
	@GetMapping("/detail")
	@ApiOperation("认证详情")
	ResponseBase detail(@RequestParam("category") Integer category, HttpServletRequest request);

	/** 保存草稿 */
	@PostMapping("/save")
	@ApiOperation("保存认证草稿")
	ResponseBase save(CreatorCertificationSaveQuery query, HttpServletRequest request);

	/** 提交审核 */
	@PostMapping("/submit")
	@ApiOperation("提交认证审核")
	ResponseBase submit(CreatorCertificationSaveQuery query, HttpServletRequest request);

	/** 审核记录时间线 */
	@GetMapping("/records")
	@ApiOperation("认证审核记录")
	ResponseBase records(HttpServletRequest request);
}
