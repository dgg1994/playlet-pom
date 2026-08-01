package com.playlet.internal.service;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.CommentLocateQuery;
import com.playlet.internal.query.drama.QueryCommentVideoQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RequestMapping("/api/comment")
@Api(value = "评论API", tags = "评论API")
public interface DramaApiVideoCommentService {
	
	/**
	 * @category 视频评论列表
	 * @param entity
	 * @return
	 */
	@PostMapping("/list")
	@ApiOperation("视频评论列表")
	ResponseBase list(QueryCommentVideoQuery entity, HttpServletRequest request);
	
	/**
	 * @category 评论回复列表
	 * @param commentId
	 * @param request
	 * @return
	 */
	@PostMapping("/reply/list")
	@ApiOperation("评论回复列表")
	ResponseBase replyList(QueryCommentVideoQuery entity, HttpServletRequest request);

	/**
	 * 精确定位评论：返回所属一级、所在页数据与 targetIndex，供前端跳转高亮。
	 */
	@PostMapping("/locate")
	@ApiOperation(value = "评论精确定位", notes = "按 commentId 返回父评、当页 siblings/parentPage；pageSize 须与 list/reply 一致")
	ResponseBase locate(CommentLocateQuery query, HttpServletRequest request);

}
