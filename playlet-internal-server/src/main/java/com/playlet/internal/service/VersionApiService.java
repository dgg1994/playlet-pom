package com.playlet.internal.service;

import com.playlet.internal.api.request.AppVersionCheckRequest;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * C端版本检查：网关 /entrance/api/version/**
 */
@RequestMapping("/api/version")
@Api(value = "版本检查", tags = "版本检查")
public interface VersionApiService {

	@PostMapping("/check")
	@ApiOperation(value = "检查更新", notes = "根据平台/渠道/当前版本号判断是否需要更新。"
			+ "也可不传 body，从请求头 x-playlet-devicetype / x-playlet-version 兜底。"
			+ "请求示例：{\"platform\":\"android\",\"channel\":\"default\",\"versionCode\":10200,\"versionName\":\"1.2.0\"}")
	ResponseBase check(AppVersionCheckRequest request, HttpServletRequest httpRequest);
}
