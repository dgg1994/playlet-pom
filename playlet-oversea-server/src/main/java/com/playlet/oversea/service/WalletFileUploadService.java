package com.playlet.oversea.service;

import com.playlet.oversea.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.oversea.api.response.WalletKycFileUploadResp;

import javax.servlet.http.HttpServletRequest;

/**
 * KYC 证件文件上传：对齐 worldPay POST /api/file/upload；网关 /entrance/api/file/**
 */
@RequestMapping("/api/file")
@Api(value = "KYC文件上传", tags = "钱包提现")
public interface WalletFileUploadService {

	@PostMapping("/upload")
	@ApiOperation(value = "单个文件上传", notes = "用于上传身份证明、护照等 KYC 证件图；multipart 字段 idCard；需登录且已开通钱包；"
			+ "返回 fileUrl 供 /wallet/kyc/apply 使用")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "idCard", value = "证件图片", required = true, dataType = "file", paramType = "form"),
			@ApiImplicitParam(name = "certType", value = "证件类型：1身份证 2护照 3驾照（可选，用于本地留痕）",
					dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "documentType", value = "文件类型：1正面 2反面 3手持/自拍（可选，用于本地留痕）",
					dataType = "int", paramType = "query")
	})
	@ApiResponses({
			@ApiResponse(code = 200, message = "成功", response = WalletKycFileUploadResp.class)
	})
	ResponseBase upload(@RequestParam("idCard") MultipartFile idCard,
			@RequestParam(value = "certType", required = false) Integer certType,
			@RequestParam(value = "documentType", required = false) Integer documentType,
			HttpServletRequest request);
}
