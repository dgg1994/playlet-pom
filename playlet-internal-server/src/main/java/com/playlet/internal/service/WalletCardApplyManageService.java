package com.playlet.internal.service;

import com.playlet.internal.api.request.WalletCardApplyRejectRequest;
import com.playlet.internal.api.request.WalletCardShippingRequest;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端银行卡申请记录（对齐 worldpay /cardApply/**）。
 * 网关：/china/admin/cardApply/**
 */
@RequestMapping("/cardApply")
@Api(value = "银行卡申请记录", tags = "银行卡申请记录")
public interface WalletCardApplyManageService {

	@PostMapping("/openCardApply")
	@ApiOperation(value = "申请记录", notes = "分页查询开卡申请；管理端可筛全量；App 带登录 token 时仅返回当前用户记录；"
			+ "含持卡人/邮寄/KYC/卡产品/已开卡信息；顶层返回 cardTitle、cardImg（七牛签名）")
	ResponseBase openCardApply(WalletCardApplyEntity entity, HttpServletRequest request);

	@GetMapping("/openCardApplyInfo")
	@ApiOperation(value = "申请记录详情", notes = "按 id 查询申请详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "申请单 id", required = true, dataType = "long", paramType = "query")
	})
	ResponseBase openCardApplyInfo(Long id);

	@GetMapping("/openCard")
	@ApiOperation(value = "开卡激活", notes = "管理端审核通过：虚拟卡调三方开卡并落本地卡记录")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "申请单 id", required = true, dataType = "long", paramType = "query")
	})
	ResponseBase openCard(Long id);

	@PostMapping("/reject")
	@ApiOperation(value = "拒绝开卡申请", notes = "拒绝申请并解冻开卡冻结金额")
	ResponseBase reject(WalletCardApplyRejectRequest entity);

	@GetMapping("/cardBinding")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "applyId", value = "申请记录 id", required = true, dataType = "long", paramType = "query"),
			@ApiImplicitParam(name = "cardNumber", value = "实体卡卡号", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "pinNum", value = "ATM PIN", required = true, dataType = "string", paramType = "query")
	})
	@ApiOperation(value = "实体卡分配激活", notes = "绑定实体卡号并调三方激活；对齐 worldpay GET /card/cardBinding")
	ResponseBase cardBinding(Long applyId, String cardNumber, String pinNum);

	@PostMapping("/shipping")
	@ApiOperation(value = "实体卡发货", notes = "首次发货：填物流单号、邮费；注册 17track；对齐 worldpay POST /card/shipping")
	ResponseBase shipping(WalletCardShippingRequest entity, HttpServletRequest request);

	@GetMapping("/upLogisticsNum")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "applyId", value = "申请记录 id", required = true, dataType = "long", paramType = "query"),
			@ApiImplicitParam(name = "logisticsNum", value = "物流单号", required = true, dataType = "string", paramType = "query")
	})
	@ApiOperation(value = "修改物流单号", notes = "对齐 worldpay GET /card/upLogisticsNum")
	ResponseBase upLogisticsNum(Long applyId, String logisticsNum);

	@GetMapping("/findLogistics")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "logisticsNum", value = "物流单号", required = true, dataType = "string", paramType = "query"),
			@ApiImplicitParam(name = "applyId", value = "申请记录 id", dataType = "long", paramType = "query")
	})
	@ApiOperation(value = "查询物流跟踪", notes = "对齐 worldpay GET /card/findLogistics")
	ResponseBase findLogistics(String logisticsNum, Long applyId);
}
