package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.welfare.WithdrawConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端提现配置：网关 /china/admin/withdrawConfigManage/**
 */
@RequestMapping("/withdrawConfigManage")
@Api(value = "提现配置管理", tags = "提现配置管理")
public interface WithdrawConfigManageService {

	@PostMapping("/findList")
	@ApiOperation(value = "提现配置分页列表", notes = "可筛 assetCode、network、status")
	ResponseBase findList(WithdrawConfigEntity entity);

	@PostMapping("/save")
	@ApiOperation(value = "新增提现配置", notes = "assetCode+network 唯一")
	ResponseBase save(WithdrawConfigEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "修改提现配置", notes = "按 id 更新；assetCode+network 唯一")
	ResponseBase update(WithdrawConfigEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation(value = "启用/停用", notes = "body: {id, status}；status=1启用 0关闭")
	ResponseBase changeStatus(WithdrawConfigEntity entity);
}
