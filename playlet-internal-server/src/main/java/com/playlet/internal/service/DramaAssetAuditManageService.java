package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.query.drama.DramaAssetAuditHandleQuery;
import com.playlet.internal.query.drama.DramaAuditHandleQuery;
import com.playlet.internal.query.drama.DramaWorkAuditQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 作品评审管理：剧（封面/简介）+ 集（视频）统一入口。
 * 网关：/china/admin/dramaAssetAuditManage/**
 */
@RequestMapping("/dramaAssetAuditManage")
@Api(value = "作品评审管理", tags = "作品评审管理")
public interface DramaAssetAuditManageService {

	@PostMapping("/findList")
	@ApiOperation("作品管理列表（仅剧：封面/简介/标签等评审对象 + 审核状态；auditStatus：0/1待审 2通过 3驳回 4申诉中）")
	ResponseBase findList(DramaWorkAuditQuery query);

	@PostMapping("/findEpisodeList")
	@ApiOperation("短剧集列表（剧名检索 + auditStatus：0/1待审 2通过 3驳回 4申诉中）")
	ResponseBase findEpisodeList(DramaWorkAuditQuery query);

	@PostMapping("/dramaDetail")
	@ApiOperation("剧评审详情（剧信息 + dramaSteps + 集列表及每集 steps）")
	ResponseBase dramaDetail(DramaEntity entity);

	@PostMapping("/dramaHandle")
	@ApiOperation("剧评审 A/B 组通过或驳回（驳回时 remark 必填）")
	ResponseBase dramaHandle(DramaAuditHandleQuery query, HttpServletRequest request);

	@PostMapping("/handle")
	@ApiOperation("集评审 A/B 组通过或驳回（驳回时 remark 必填）")
	ResponseBase handle(DramaAssetAuditHandleQuery query, HttpServletRequest request);
}
