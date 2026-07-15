package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.RankBoardEntity;
import com.playlet.internal.entity.drama.RankListEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端榜单维护：网关 /china/admin/rankManage/**
 */
@RequestMapping("/rankManage")
@Api(value = "榜单管理", tags = "榜单管理")
public interface RankManageService {

	@PostMapping("/board/findList")
	@ApiOperation("榜单定义分页")
	ResponseBase boardFindList(RankBoardEntity entity);

	@GetMapping("/board/detail")
	@ApiOperation("榜单定义详情")
	ResponseBase boardDetail(Integer id);

	@PostMapping("/board/save")
	@ApiOperation("新增榜单定义")
	ResponseBase boardSave(RankBoardEntity entity);

	@PostMapping("/board/update")
	@ApiOperation("编辑榜单定义")
	ResponseBase boardUpdate(RankBoardEntity entity);

	@PostMapping("/board/changeStatus")
	@ApiOperation("启用/停用榜单")
	ResponseBase boardChangeStatus(RankBoardEntity entity);

	@GetMapping("/board/delete")
	@ApiOperation("删除榜单定义（同时清空列表）")
	ResponseBase boardDelete(Integer id);

	@PostMapping("/list/findList")
	@ApiOperation("榜单条目分页")
	ResponseBase listFindList(RankListEntity entity);

	@GetMapping("/list/detail")
	@ApiOperation("榜单条目详情")
	ResponseBase listDetail(Integer id);

	@PostMapping("/list/save")
	@ApiOperation("新增榜单条目")
	ResponseBase listSave(RankListEntity entity);

	@PostMapping("/list/update")
	@ApiOperation("编辑榜单条目")
	ResponseBase listUpdate(RankListEntity entity);

	@PostMapping("/list/changeStatus")
	@ApiOperation("启用/停用条目")
	ResponseBase listChangeStatus(RankListEntity entity);

	@PostMapping("/list/replaceAll")
	@ApiOperation("整榜覆盖（boardCode + items，适合人工调序）")
	ResponseBase listReplaceAll(RankListEntity entity);
}
