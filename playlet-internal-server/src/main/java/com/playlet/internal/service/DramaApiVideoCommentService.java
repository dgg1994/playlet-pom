package com.playlet.internal.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.QueryCommentVideoQuery;

import io.swagger.annotations.Api;

@RequestMapping("/api/comment")
@Api(value = "评论API", tags = "评论API")
public interface DramaApiVideoCommentService {
	
	/**
	 * @category 视频评论列表
	 * @param entity
	 * @return
	 */
	@PostMapping("/list")
	ResponseBase list(QueryCommentVideoQuery entity);

}
