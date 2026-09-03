package com.playlet.oversea.service;

import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.oversea.base.ResponseBase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/dic")
@Tag(name = "字典",description = "字典")
public interface DicService {

	@GetMapping("/getLanguage")
	@Operation(summary = "查询语言列表", description = "查询语言列表")
	ResponseBase getLanguage();
	
	@GetMapping("/findUserState")
	@Operation(summary = "查询用户状态", description = "查询用户状态")
	ResponseBase findUserState();
	
	@GetMapping("/findDeviceType")
	@Operation(summary = "查询设备类型", description = "查询设备类型")
	ResponseBase findDeviceType();
	
	@GetMapping("/findOrderState")
	@Operation(summary = "查询订单状态", description = "查询订单状态")
	ResponseBase findOrderState();
	
	@GetMapping("/findVerifyStatus")
	@Operation(summary = "查询审核状态类型", description = "查询审核状态类型")
	ResponseBase findVerifyStatus();

	@GetMapping("/findWelfareActionType")
	@Operation(summary = "查询每日任务类型", description = "查询每日任务类型")
	ResponseBase findWelfareActionType();

	@GetMapping("/findWelfareCycleType")
	@Operation(summary = "查询福利任务周期类型", description = "查询福利任务周期类型")
	ResponseBase findWelfareCycleType();

	@GetMapping("/findProtocolType")
	@ApiOperation(value = "查询协议类型",notes = "查询协议类型",response = ResponseBase.class)
	ResponseBase findProtocolType();

	@GetMapping("/findCardState")
	@Operation(summary = "查询卡状态", description = "用户持卡/开卡申请卡状态下拉")
	ResponseBase findCardState();

	@GetMapping("/findPayType")
	@Operation(summary = "查询充值方式", description = "1钱包 2银行卡")
	ResponseBase findPayType();

	@GetMapping("/findKycState")
	@Operation(summary = "查询KYC状态", description = "KYC认证状态下拉")
	ResponseBase findKycState();

	@GetMapping("/findNetwokList")
	@Operation(summary = "查询网络类型", description = "TRON/BSC等链网络")
	ResponseBase findNetwokList();

	@GetMapping("/findRecordState")
	@Operation(summary = "查询审核状态", description = "提现审核状态下拉")
	ResponseBase findRecordState();
}

