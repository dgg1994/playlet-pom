package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端：创作者身份资质审核。
 */
@RequestMapping("/creatorIdentityManage")
@Api(value = "管理端-创作者身份资质审核", tags = "管理端-创作者身份资质审核")
public interface CreatorIdentityManageService {

	/** 身份资质审核列表 */
	@PostMapping("/findList")
	@ApiOperation("身份资质审核列表")
	ResponseBase findList(@RequestParam(value = "keyword", required = false) String keyword,
						  @RequestParam(value = "identityType", required = false) Integer identityType,
						  @RequestParam(value = "status", required = false) Integer status,
						  @RequestParam(value = "startTime", required = false) String startTime,
						  @RequestParam(value = "endTime", required = false) String endTime,
						  @RequestParam(value = "pageNumber", defaultValue = "1") Integer pageNumber,
						  @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize);

	/** 审核（通过 / 驳回） */
	@PostMapping("/audit")
	@ApiOperation("身份资质审核")
	ResponseBase audit(@RequestParam("id") Integer id,
					  @RequestParam("auditStatus") Integer auditStatus,
					  @RequestParam(value = "rejectReason", required = false) String rejectReason,
					  @RequestParam(value = "auditorId", required = false) Integer auditorId);
}
