package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorDramaListQuery;
import com.playlet.internal.query.drama.DramaAppealQuery;
import com.playlet.internal.query.drama.DramaAssetAppealQuery;
import com.playlet.internal.query.drama.DramaAssetBatchShelfQuery;
import com.playlet.internal.query.drama.DramaAssetShelfQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端作品：列表/详情/申诉/集上下架。
 */
@RequestMapping("/creator/drama")
@Api(value = "作家端作品", tags = "作家端作品")
public interface CreatorDramaService {

	@PostMapping("/findList")
	@ApiOperation(value = "我的作品列表", notes = "剧卡片：封面/标题/总集数/审核状态/驳回原因/更新时间。剧集走 GET /findInfo?id=")
	ResponseBase findList(CreatorDramaListQuery query, HttpServletRequest request);

	@GetMapping("/findInfo")
	@ApiImplicitParam(name = "id", value = "剧ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "作品详情", notes = "选中某剧后调用；assetList 为剧集表（序列/时长/状态/曝光/完播/上传日期）")
	ResponseBase findInfo(@RequestParam("id") Integer id, HttpServletRequest request);

	@PostMapping("/appeal")
	@ApiOperation(value = "剧申诉", notes = "仅驳回后可发起；auditStatus=4 申诉中并重置 AI/A/B 进入再审；校验 belongUser。"
			+ "运营列表传 auditStatus=4 可见。")
	ResponseBase appeal(DramaAppealQuery query, HttpServletRequest request);

	@PostMapping("/assetAppeal")
	@ApiOperation(value = "集申诉", notes = "仅驳回后可发起；auditStatus=4 申诉中并重置 AI/A/B 进入再审；校验所属剧 belongUser。"
			+ "运营列表传 auditStatus=4 可见。")
	ResponseBase assetAppeal(DramaAssetAppealQuery query, HttpServletRequest request);

	@PostMapping("/shelf")
	@ApiOperation(value = "集上架/下架", notes = "body: {assetId, shelfStatus}；shelfStatus=1 上架（集与剧均须过审，过审后可自动上架整剧），"
			+ "0 下架（无上架集时自动下架整剧）。校验 belongUser。")
	ResponseBase shelf(DramaAssetShelfQuery query, HttpServletRequest request);

	@PostMapping("/batchShelf")
	@ApiOperation(value = "批量集上架/下架", notes = "body: {assetIds, shelfStatus}；shelfStatus=1 上架 / 0 下架；"
			+ "逐条校验归属/审核；部分失败时返回成功数与失败明细；每部剧只同步一次整剧上架状态。")
	ResponseBase batchShelf(DramaAssetBatchShelfQuery query, HttpServletRequest request);
}
