package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.message.SystemMessagePublishEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端系统消息：网关 /china/admin/systemMessageManage/**
 */
@RequestMapping("/systemMessageManage")
@Api(value = "系统消息管理", tags = "系统消息管理")
public interface SystemMessageManageService {

	@PostMapping("/findList")
	@ApiOperation("发布单分页列表")
	ResponseBase findList(SystemMessagePublishEntity entity);

	@PostMapping("/detail")
	@ApiOperation("发布单详情（含多语言）")
	ResponseBase detail(SystemMessagePublishEntity entity);

	@PostMapping("/save")
	@ApiOperation("新增草稿（含多语言）")
	ResponseBase save(SystemMessagePublishEntity entity);

	@PostMapping("/update")
	@ApiOperation("编辑（草稿可改；i18n 全量覆盖）")
	ResponseBase update(SystemMessagePublishEntity entity);

	@PostMapping("/publish")
	@ApiOperation("发布：全员读扩散直接上线；指定uid写收件箱")
	ResponseBase publish(SystemMessagePublishEntity entity);

	@PostMapping("/cancel")
	@ApiOperation("取消发布")
	ResponseBase cancel(SystemMessagePublishEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation("上架/下架")
	ResponseBase changeStatus(SystemMessagePublishEntity entity);
}
