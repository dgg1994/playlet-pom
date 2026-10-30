package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.query.drama.DramaAssetAuditHandleQuery;
import com.playlet.internal.query.drama.DramaAuditHandleQuery;
import com.playlet.internal.query.drama.DramaRecordAuditQuery;
import com.playlet.internal.query.drama.DramaRecordHandleQuery;
import com.playlet.internal.query.drama.DramaWorkAuditQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 作品评审管理：剧（封面/简介）+ 集（视频）统一入口；
 * 备案审核：二类/三类短剧备案资质材料审核（初审→复审）。
 * 网关：/china/admin/dramaAssetAuditManage/**
 */
@RequestMapping("/dramaAssetAuditManage")
@Api(value = "作品评审管理", tags = "作品评审管理")
public interface DramaAssetAuditManageService {

	// ===================== 作品评审 =====================

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

	// ===================== 备案审核 =====================

	@PostMapping("/findRecordList")
	@ApiOperation("备案审核列表（keyword 剧名/出品方模糊检索；recordStatus 备案状态；filingCategory 分类）")
	ResponseBase findRecordList(DramaRecordAuditQuery query);

	@PostMapping("/recordDetail")
	@ApiOperation("备案审核详情（剧基础信息 + 集列表 + 材料 + 审核步骤 + 审核日志）")
	ResponseBase recordDetail(DramaEntity entity);

	@PostMapping("/recordHandle")
	@ApiOperation("备案审核处理（stepType=1初审/2复审，action=1通过/2驳回；驳回时 remark 必填）")
	ResponseBase recordHandle(DramaRecordHandleQuery query, HttpServletRequest request);
}
