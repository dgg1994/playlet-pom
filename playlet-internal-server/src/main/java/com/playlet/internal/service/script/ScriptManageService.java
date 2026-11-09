package com.playlet.internal.service.script;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.script.ScriptAuditQuery;
import com.playlet.internal.query.script.ScriptManageListQuery;
import com.playlet.internal.query.script.ScriptShelfQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 管理端剧本管理：列表、审核、强制上下架。
 */
@RequestMapping("/scriptManage")
@Api(value = "管理端剧本", tags = "管理端剧本")
public interface ScriptManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "剧本管理列表", notes = "支持标题/作者/审核/上架/售卖/时间范围筛选")
	ResponseBase findList(@RequestBody ScriptManageListQuery query);

	@PostMapping("/audit")
	@ApiOperation(value = "剧本审核", notes = "2 通过；3 驳回且 rejectReason 必填")
	ResponseBase audit(@Valid @RequestBody ScriptAuditQuery query, HttpServletRequest request);

	@PostMapping("/shelf")
	@ApiOperation(value = "强制上下架", notes = "写审核日志，保留原因")
	ResponseBase shelf(@Valid @RequestBody ScriptShelfQuery query, HttpServletRequest request);
}
