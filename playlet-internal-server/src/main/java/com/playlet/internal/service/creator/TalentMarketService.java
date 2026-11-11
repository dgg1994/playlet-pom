package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.TalentMarketListQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 人才市场：找编剧 / 找创作者浏览。
 */
@RequestMapping("/api/talent")
@Api(value = "人才市场", tags = "人才市场")
public interface TalentMarketService {

	@GetMapping("/market/list")
	@ApiOperation(value = "人才市场列表", notes = "仅返回已上架数据；支持关键词、6 组筛选、6 种排序、分页")
	ResponseBase list(TalentMarketListQuery query, HttpServletRequest request);

	@GetMapping("/market/detail")
	@ApiImplicitParam(name = "id", value = "人才ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "人才详情", notes = "已下架/不存在返回 404；详情区块为空时按模板兜底生成")
	ResponseBase detail(@RequestParam("id") Integer id, HttpServletRequest request);
}
