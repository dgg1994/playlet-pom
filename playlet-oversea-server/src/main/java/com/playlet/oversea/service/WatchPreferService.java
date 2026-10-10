package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 类描述：喜好分类接口
 *
 * @author GeminiSun
 * @date 2026/10/10 10:31
 */
@RequestMapping("/api/pref")
@Api(value = "偏好设置", tags = "偏好设置")
public interface WatchPreferService {

    @GetMapping("/list")
    @ApiOperation(value = "查看偏好列表")
    ResponseBase list(HttpServletRequest request);
}
