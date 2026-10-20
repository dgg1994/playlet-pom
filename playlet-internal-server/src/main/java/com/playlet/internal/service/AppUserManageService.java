package com.playlet.internal.service;

import com.playlet.internal.api.request.AppUserFreezeQuery;
import com.playlet.internal.api.request.AppUserMuteQuery;
import com.playlet.internal.api.request.AppUserNotifyQuery;
import com.playlet.internal.api.request.AppUserProfileUpdateQuery;
import com.playlet.internal.api.request.AppUserResetPwdQuery;
import com.playlet.internal.api.request.AppUserUidPageQuery;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.account.AppUserManageQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理端 C 端用户管理：网关 /china/admin/appUserManage/**
 */
@RequestMapping("/appUserManage")
@Api(value = "App用户管理", tags = "App用户管理")
public interface AppUserManageService {

	@GetMapping("/stats")
	@ApiOperation("顶部统计：总注册/今日新增/付费用户/有效会员")
	ResponseBase stats();

	@PostMapping("/findList")
	@ApiOperation("用户分页列表")
	ResponseBase findList(AppUserManageQuery query);

	@GetMapping("/detail")
	@ApiImplicitParam(name = "uid", value = "用户ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation("用户详情")
	ResponseBase detail(Integer uid);

	@PostMapping("/updateProfile")
	@ApiOperation("编辑资料")
	ResponseBase updateProfile(AppUserProfileUpdateQuery query, HttpServletRequest request);

	@PostMapping("/resetPassword")
	@ApiOperation("重置登录密码；未传新密码则生成临时密码并返回")
	ResponseBase resetPassword(AppUserResetPwdQuery query, HttpServletRequest request);

	@PostMapping("/mute")
	@ApiOperation("临时禁言")
	ResponseBase mute(AppUserMuteQuery query, HttpServletRequest request);

	@PostMapping("/unmute")
	@ApiOperation("解除禁言")
	ResponseBase unmute(AppUserMuteQuery query, HttpServletRequest request);

	@PostMapping("/freeze")
	@ApiOperation("冻结账号（踢下线）")
	ResponseBase freeze(AppUserFreezeQuery query, HttpServletRequest request);

	@PostMapping("/unfreeze")
	@ApiOperation("解冻账号")
	ResponseBase unfreeze(AppUserFreezeQuery query, HttpServletRequest request);

	@PostMapping("/sendNotify")
	@ApiOperation("发送站内通知")
	ResponseBase sendNotify(AppUserNotifyQuery query, HttpServletRequest request);

	@PostMapping("/statusLogs")
	@ApiOperation("账号状态流水")
	ResponseBase statusLogs(AppUserUidPageQuery query);

	@PostMapping("/rechargeOrders")
	@ApiOperation("充值/会员支付记录（已支付会员订单）")
	ResponseBase rechargeOrders(AppUserUidPageQuery query);

	@PostMapping("/watchHistory")
	@ApiOperation("观看记录")
	ResponseBase watchHistory(AppUserUidPageQuery query);

	@PostMapping("/feedbackList")
	@ApiOperation("反馈记录（暂无表，返回空分页）")
	ResponseBase feedbackList(AppUserUidPageQuery query);

	@PostMapping("/export")
	@ApiOperation("导出当前筛选用户 CSV")
	void export(AppUserManageQuery query, HttpServletResponse response);
}
