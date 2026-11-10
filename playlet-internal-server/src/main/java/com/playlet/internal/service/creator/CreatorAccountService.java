package com.playlet.internal.service.creator;

import com.playlet.internal.api.response.*;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 账号信息相关接口（聚合 / 联系方式 / 身份资质 / 设置 / 注销）。
 */
@RequestMapping("/api/creator")
@Api(value = "作家端账号信息", tags = "作家端账号信息")
public interface CreatorAccountService {

	/** 账号信息聚合（账号信息页 5 个卡片一次性返回） */
	@GetMapping("/account/detail")
	@ApiOperation("账号信息聚合")
	ResponseBase detail(HttpServletRequest request);

	/** 更换邮箱 */
	@PostMapping("/account/email/change")
	@ApiOperation("更换邮箱")
	ResponseBase changeEmail(CreatorEmailChangeQuery query, HttpServletRequest request);

	/** 更换 / 绑定手机号 */
	@PostMapping("/account/mobile/change")
	@ApiOperation("更换手机号")
	ResponseBase changeMobile(CreatorMobileChangeQuery query, HttpServletRequest request);

	/** 微信绑定：获取二维码 */
	@GetMapping("/account/wechat/qrcode")
	@ApiOperation("微信绑定二维码")
	ResponseBase wechatQrcode(HttpServletRequest request);

	/** 微信绑定：轮询状态 */
	@GetMapping("/account/wechat/status")
	@ApiOperation("微信绑定状态")
	ResponseBase wechatStatus(@RequestParam("scene") String scene, HttpServletRequest request);

	/** 微信解绑 */
	@PostMapping("/account/wechat/unbind")
	@ApiOperation("微信解绑")
	ResponseBase wechatUnbind(HttpServletRequest request);

	/** 身份资质列表（4 类） */
	@GetMapping("/account/identities")
	@ApiOperation("身份资质列表")
	ResponseBase identities(HttpServletRequest request);

	/** 身份资质详情 */
	@GetMapping("/account/identity/detail")
	@ApiOperation("身份资质详情")
	ResponseBase identityDetail(@RequestParam("identityType") Integer identityType, HttpServletRequest request);

	/** 提交身份认证 */
	@PostMapping("/identity/apply")
	@ApiOperation("提交身份认证")
	ResponseBase applyIdentity(CreatorIdentityApplyQuery query, HttpServletRequest request);

	/** 隐私 + 推送设置 */
	@GetMapping("/account/setting")
	@ApiOperation("隐私+推送设置")
	ResponseBase setting(HttpServletRequest request);

	/** 保存设置（部分字段） */
	@PostMapping("/account/setting/save")
	@ApiOperation("保存设置")
	ResponseBase saveSetting(CreatorSettingSaveQuery query, HttpServletRequest request);

	/** 注销状态 */
	@GetMapping("/account/cancel/status")
	@ApiOperation("注销申请状态")
	ResponseBase cancelStatus(HttpServletRequest request);

	/** 申请注销 */
	@PostMapping("/account/cancel/apply")
	@ApiOperation("申请注销")
	ResponseBase applyCancel(CreatorCancelApplyQuery query, HttpServletRequest request);

	/** 撤销注销 */
	@PostMapping("/account/cancel/revoke")
	@ApiOperation("撤销注销申请")
	ResponseBase revokeCancel(HttpServletRequest request);
}
