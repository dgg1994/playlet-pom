package com.playlet.internal.service.script;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.script.CreatorScriptApplyListQuery;
import com.playlet.internal.query.script.CreatorScriptListQuery;
import com.playlet.internal.query.script.ScriptApplyHandleQuery;
import com.playlet.internal.query.script.ScriptIdQuery;
import com.playlet.internal.query.script.ScriptSaveQuery;
import com.playlet.internal.query.script.ScriptShelfQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 作家端剧本管理：发布、编辑、上下架、删除、处理申请。
 */
@RequestMapping("/creator/script")
@Api(value = "作家端剧本", tags = "作家端剧本")
public interface CreatorScriptService {

	@PostMapping("/save")
	@ApiOperation(value = "新增/编辑剧本", notes = "id 为空新增；人物/分集/试读全量覆盖；返回 {id}")
	ResponseBase save(@Valid @RequestBody ScriptSaveQuery query, HttpServletRequest request);

	@GetMapping("/findList")
	@ApiOperation(value = "我的剧本列表", notes = "归属由登录态注入，禁止客户端传 authorId")
	ResponseBase findList(CreatorScriptListQuery query, HttpServletRequest request);

	@GetMapping("/findInfo")
	@ApiImplicitParam(name = "id", value = "剧本ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "我的剧本详情", notes = "返回市场详情结构 + 审核状态/驳回原因")
	ResponseBase findInfo(@RequestParam("id") Integer id, HttpServletRequest request);

	@PostMapping("/shelf")
	@ApiOperation(value = "上架/下架", notes = "shelfStatus=1 需审核通过；0 立即下架")
	ResponseBase shelf(@Valid @RequestBody ScriptShelfQuery query, HttpServletRequest request);

	@PostMapping("/delete")
	@ApiOperation(value = "删除剧本", notes = "软删除；存在待处理申请时禁止删除")
	ResponseBase delete(@Valid @RequestBody ScriptIdQuery query, HttpServletRequest request);

	@GetMapping("/applyList")
	@ApiOperation(value = "收到的交易申请", notes = "购买方联系电话脱敏")
	ResponseBase applyList(CreatorScriptApplyListQuery query, HttpServletRequest request);

	@PostMapping("/apply/handle")
	@ApiOperation(value = "处理交易申请", notes = "同意：剧本签约中并失效其他申请；拒绝：无其他待处理则回退待售")
	ResponseBase applyHandle(@Valid @RequestBody ScriptApplyHandleQuery query, HttpServletRequest request);
}
