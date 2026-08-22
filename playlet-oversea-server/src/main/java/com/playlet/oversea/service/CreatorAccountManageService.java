package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.query.creator.CreatorAccountManageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端创作者用户管理：网关 /global/admin/creatorAccountManage/**
 */
@RequestMapping("/creatorAccountManage")
@Api(value = "创作者用户管理", tags = "创作者用户管理")
public interface CreatorAccountManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "创作者用户分页列表", notes = "支持邮箱/昵称/手机号、账号状态、入驻审核筛选")
	ResponseBase findList(CreatorAccountManageQuery query);

	@GetMapping("/delete")
	@ApiImplicitParam(name = "id", value = "creator_account.id", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "删除创作者用户", notes = "软删除：账号置为注销并踢下线；有进行中提现或冻结金币时不允许")
	ResponseBase delete(Integer id, HttpServletRequest request);
}
