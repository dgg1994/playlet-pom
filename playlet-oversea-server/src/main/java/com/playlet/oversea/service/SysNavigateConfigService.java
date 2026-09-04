package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.system.SysNavigateConfigEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/sysNavigateConfig")
@Api(value = "导航开启隐藏配置",tags = "导航开启隐藏配置")
public interface SysNavigateConfigService {

	@GetMapping("/findList")
	@ApiOperation(value = "查询配置", notes = "按库值返回开关：1→true，其它→false", response = ResponseBase.class)
    ResponseBase findList();
	
	@PostMapping("/update")
	@ApiOperation(value = "编辑配置", notes = "编辑配置", response = ResponseBase.class)
	ResponseBase update(SysNavigateConfigEntity configEntity);
	
	@PostMapping("/add")
	@ApiOperation(value = "新增配置", notes = "新增配置", response = ResponseBase.class)
	ResponseBase add(SysNavigateConfigEntity configEntity);
	
	@GetMapping("/delete")
	@ApiOperation(value = "删除配置", notes = "删除配置", response = ResponseBase.class)
	ResponseBase delete(Integer id);
	
	@GetMapping("/find")
	@ApiOperation(value = "查询配置", notes = "查询配置", response = ResponseBase.class)
	ResponseBase find();
	
}
