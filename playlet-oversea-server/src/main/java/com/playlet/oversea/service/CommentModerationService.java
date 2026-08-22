package com.playlet.oversea.service;

import com.playlet.oversea.entity.security.IllegalCommentRecordEntity;

/**
 * 评论审核通过/删除后的业务副作用。
 */
public interface CommentModerationService {

	/**
	 * 审核通过：恢复隐藏评论并补发计分/推送/勋章等副作用。
	 */
	void approveHiddenComment(IllegalCommentRecordEntity record) throws Exception;

	/**
	 * 删除评论：软删除并回退可见评论的计分。
	 */
	void deleteComment(Integer commentId) throws Exception;
}
