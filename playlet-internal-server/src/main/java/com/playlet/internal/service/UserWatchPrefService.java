package com.playlet.internal.service;

import com.playlet.internal.api.request.WatchPreferSaveRequest;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 类描述：用户看剧喜好设置
 *
 * @author GeminiSun
 * @date 2026/10/10 10:31
 */
@RequestMapping("/userWatch")
@Api(value = "用户偏好", tags = "用户偏好")
public interface UserWatchPrefService {

	@PostMapping("/save")
	@ApiOperation(value = "首次设置用户偏好", notes = "需登录；按分类提交 preferGroupIds，全量覆盖写入")
	ResponseBase save(@RequestBody WatchPreferSaveRequest watchPreferSaveRequest, HttpServletRequest request);

	@PostMapping("/update")
	@ApiOperation(value = "修改用户偏好", notes = "需登录；须已设置过偏好；入参与 save 相同，全量覆盖（先删后插）")
	ResponseBase update(@RequestBody WatchPreferSaveRequest watchPreferSaveRequest, HttpServletRequest request);

	@GetMapping("/mine")
	@ApiOperation(value = "查询我的偏好（编辑回显）", notes = "需登录；返回分类+选项，并标记 selected")
	ResponseBase mine(HttpServletRequest request);

}
