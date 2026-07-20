package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.welfare.WelfareTaskEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端福利任务：网关 /china/admin/welfareTask/**
 */
@RequestMapping("/welfareTask")
@Api(value = "福利任务管理", tags = "福利任务管理")
public interface WelfareTaskManageService {

	@PostMapping("/findList")
	@ApiOperation("任务分页列表")
	ResponseBase findList(WelfareTaskEntity entity);

	@PostMapping("/detail")
	@ApiOperation("任务详情（含多语言）")
	ResponseBase detail(WelfareTaskEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增任务")
	ResponseBase save(WelfareTaskEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑任务（不可改 taskCode；i18n 全量覆盖）")
	ResponseBase update(WelfareTaskEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("启用/停用")
	ResponseBase changeStatus(WelfareTaskEntity entity);

	@PostMapping("/delete")
	@ApiOperation("删除任务配置及多语言（不删用户进度/流水）")
	ResponseBase delete(WelfareTaskEntity entity);
}
