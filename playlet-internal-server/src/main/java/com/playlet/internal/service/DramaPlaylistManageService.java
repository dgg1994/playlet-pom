package com.playlet.internal.service;

import com.playlet.internal.api.request.DramaPlaylistRequestEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.DramaPlaylistEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端剧单维护：网关 /china/admin/dramaPlaylistManage/**
 */
@RequestMapping("/dramaPlaylistManage")
@Api(value = "剧单管理", tags = "剧单管理")
public interface DramaPlaylistManageService {

	@PostMapping("/findList")
	@ApiOperation("剧单分页列表")
	ResponseBase findList(DramaPlaylistEntity entity);

	@GetMapping("/detail")
	@ApiOperation("剧单详情（含绑定标签）")
	ResponseBase detail(Integer id);

	@PostMapping("/save")
	@ApiOperation("新增剧单（多语言 + 标签）")
	ResponseBase save(DramaPlaylistRequestEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑剧单（多语言 upsert + 标签全量替换）")
	ResponseBase update(DramaPlaylistRequestEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用剧单（按 groupId）")
	ResponseBase changeStatus(DramaPlaylistEntity entity);

	@GetMapping("/delete")
	@ApiOperation("删除剧单语言行；同 group 无其它行时清空标签")
	ResponseBase delete(Integer id);
}
