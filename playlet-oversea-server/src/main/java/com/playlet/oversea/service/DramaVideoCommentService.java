package com.playlet.oversea.service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.query.drama.AddDramaVideoCommentQuery;
import com.playlet.oversea.query.drama.CommentGiveLikeQuery;
import com.playlet.oversea.query.drama.ReplyVideoCommentQuery;

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
	
	/**
	 * @category 评论/回复点赞、取消点赞
	 * @param giveLikeQuery
	 * @return
	 */
	@PostMapping("/giveLike")
	ResponseBase giveLike(CommentGiveLikeQuery giveLikeQuery);
	
	/**
	 * @category 删除评论/回复
	 * @param id
	 * @return
	 */
	@GetMapping("/delete")
	ResponseBase delete(Integer id);

}
