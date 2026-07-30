package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 类描述：用户配置接口
 *
 * @author GeminiSun
 * @date 2026/07/30 10:49
 */
@RequestMapping("/api/info")
@Api(value = "配置接口",tags = "配置接口")
public interface SysInfoApiService {

    @GetMapping("/findConfigInfo")
    @ApiOperation(value = "查询配置（1：用户协议；2：隐私协议；3：关于我们；4：联系我们；5：客服；）",notes = "查询配置",response = ResponseBase.class)
    ResponseBase findConfigInfo(Integer configType);
}
