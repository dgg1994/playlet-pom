package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.creator.CreatorCommentListQuery;
import com.playlet.internal.query.creator.CreatorCommentPinQuery;
import com.playlet.internal.query.creator.CreatorCommentReplyQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端评论管理。
 * 网关：/china/admin/api/creator/comment/** 或 /entrance/api/creator/comment/**
 */
@RequestMapping("/creator/comment")
@Api(value = "作家端评论", tags = "作家端评论")
public interface CreatorCommentService {

	@PostMapping("/findList")
	@ApiOperation(value = "评论列表", notes = "本人作品评论；支持 dramaId、commentType(1集评/2剧评，默认1)、剧名搜索、热度/时间排序；置顶优先")
	ResponseBase findList(CreatorCommentListQuery query, HttpServletRequest request);

	@PostMapping("/pin")
	@ApiOperation(value = "置顶/取消置顶", notes = "仅本人作品下的评论")
	ResponseBase pin(CreatorCommentPinQuery query, HttpServletRequest request);

	@GetMapping("/delete")
	@ApiImplicitParam(name = "id", value = "评论ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "删除评论", notes = "软删；校验作品归属")
	ResponseBase delete(@RequestParam("id") Integer id, HttpServletRequest request);

	@PostMapping("/reply")
	@ApiOperation(value = "回复评论", notes = "commentType=1 剧集评（需 videoId）/ 2 短剧评；作者身份写入 from_creator_id，不可评分")
	ResponseBase reply(CreatorCommentReplyQuery query, HttpServletRequest request);
}
