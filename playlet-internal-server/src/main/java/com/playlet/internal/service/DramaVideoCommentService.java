package com.playlet.internal.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaVideoCommentQuery;
import com.playlet.internal.query.drama.ReplyVideoCommentQuery;

import io.swagger.annotations.Api;

@RequestMapping("/comment")
@Api(value = "评论", tags = "评论")
public interface DramaVideoCommentService {
	
	/**
	 * @category 发表评论
	 * @param entity
	 * @return
	 */
	@PostMapping("/publish")
	ResponseBase publish(AddDramaVideoCommentQuery entity);
	
	/**
	 * @category 回复评论
	 * @param entity
	 * @return
	 */
	@PostMapping("/reply")
	ResponseBase reply(ReplyVideoCommentQuery entity);
	
//	
//	@PostMapping("/reply")
//	ResponseBase reply(ReplyVideoCommentQuery entity);

}
