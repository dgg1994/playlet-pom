package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaCommentQuery;
import com.playlet.internal.query.drama.CommentGiveLikeQuery;
import com.playlet.internal.query.drama.ReplyDramaCommentQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 短剧评论写接口（共用 drama_video_comment，comment_type=2）
 * 对应视频评论 /comment/**
 */
@RequestMapping("/dramaComment")
@Api(value = "短剧评论", tags = "短剧评论")
public interface DramaCommentService {

	@PostMapping("/publish")
	@ApiOperation("发表/更新短剧评论（发布人可评分1-5）")
	ResponseBase publish(AddDramaCommentQuery entity);

	@PostMapping("/reply")
	@ApiOperation("回复短剧评论（不可评分）")
	ResponseBase reply(ReplyDramaCommentQuery entity);

	@PostMapping("/giveLike")
	@ApiOperation("评论/回复点赞、取消点赞")
	ResponseBase giveLike(CommentGiveLikeQuery giveLikeQuery);

	@GetMapping("/delete")
	@ApiOperation("删除评论/回复")
	ResponseBase delete(Integer id);
}
