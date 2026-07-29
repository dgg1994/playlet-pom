package com.playlet.internal.service;


import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.system.SysInfoEntity;
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
	ResponseBase add(SysInfoEntity entity);

	@PostMapping("/update")
	@ApiOperation(value = "更新配置",notes = "更新配置",response = ResponseBase.class)
	ResponseBase update(SysInfoEntity entity);

    @GetMapping("/findConfigInfo")
	@ApiOperation(value = "查询配置（1：用户协议；2：隐私协议；3：关于我们；4：联系我们；5：客服；）",notes = "查询配置",response = ResponseBase.class)
    ResponseBase findConfigInfo(Integer configType);
	
}
