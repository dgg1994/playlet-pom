package com.playlet.oversea.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.query.drama.DramaCommentLocateQuery;
import com.playlet.oversea.query.drama.QueryDramaCommentQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 短剧评论读接口（C端）
 * 对应视频评论 /api/comment/**
 */
@RequestMapping("/api/dramaComment")
@Api(value = "短剧评论API", tags = "短剧评论API")
public interface DramaApiDramaCommentService {

	@PostMapping("/list")
	@ApiOperation("短剧评论列表")
	ResponseBase list(QueryDramaCommentQuery entity, HttpServletRequest request);

	@PostMapping("/detail")
	@ApiOperation("短剧评论详情")
	ResponseBase detail(QueryDramaCommentQuery entity, HttpServletRequest request);

	@PostMapping("/reply/list")
	@ApiOperation("短剧评论回复列表")
	ResponseBase replyList(QueryDramaCommentQuery entity, HttpServletRequest request);

	@PostMapping("/scoreSummary")
	@ApiOperation("评分汇总")
	ResponseBase scoreSummary(QueryDramaCommentQuery entity);

	@PostMapping("/mine")
	@ApiOperation("我对该剧的评论（含评分）")
	ResponseBase mine(QueryDramaCommentQuery entity, HttpServletRequest request);

	@PostMapping("/locate")
	@ApiOperation(value = "短剧评论精确定位", notes = "按 commentId 返回父评、当页 siblings/parentPage；pageSize 须与 list/reply 一致")
	ResponseBase locate(DramaCommentLocateQuery query, HttpServletRequest request);
}
