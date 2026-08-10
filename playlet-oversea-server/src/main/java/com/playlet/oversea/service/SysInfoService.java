package com.playlet.oversea.service;


import com.playlet.oversea.api.request.SysInfoAddEntity;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.entity.system.SysInfoEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *  @category 配置接口
 */
@RequestMapping("/info")
@Api(value = "配置接口",tags = "配置接口")
public interface SysInfoService {

	@PostMapping("/findAll")
	@ApiOperation(value = "查询所有配置",notes = "查询所有配置",response = ResponseBase.class)
	ResponseBase findAll(SysInfoEntity entity);

	@GetMapping("/findById")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "主键id", required = true, dataType = "int", paramType = "query"),
	})
	@ApiOperation(value = "id查询配置",notes = "查询配置",response = ResponseBase.class)
	ResponseBase findById(Integer id);

	@PostMapping("/add")
	@ApiOperation(value = "新增配置",notes = "新增配置",response = ResponseBase.class)
	ResponseBase add(SysInfoAddEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "更新配置",notes = "按 configType 批量更新多语言配置",response = ResponseBase.class)
	ResponseBase update(SysInfoAddEntity entity);

	@PostMapping("/changeStatus")
	@ApiOperation(value = "启停配置", notes = "启停配置（1正常 2停用）", response = ResponseBase.class)
	ResponseBase changeStatus(SysInfoEntity entity);

}
