package com.playlet.internal.service;

import com.playlet.internal.api.request.WalletCardApplyRejectRequest;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.wallet.WalletCardApplyEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端银行卡申请记录（对齐 worldpay /cardApply/**）。
 * 网关：/china/admin/cardApply/**
 */
@RequestMapping("/cardApply")
@Api(value = "银行卡申请记录", tags = "银行卡申请记录")
public interface WalletCardApplyManageService {

	@PostMapping("/openCardApply")
	@ApiOperation(value = "申请记录", notes = "分页查询开卡申请；含持卡人/邮寄/KYC/卡产品/已开卡信息")
	ResponseBase openCardApply(WalletCardApplyEntity entity);

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
}
