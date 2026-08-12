package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.security.IllegalCommentRecordEntity;
import com.playlet.internal.query.security.IllegalCommentHandleQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理端违规评论后置处置：网关 /china/admin/illegalCommentManage/**
 */
@RequestMapping("/illegalCommentManage")
@Api(value = "违规评论管理", tags = "违规评论管理")
public interface IllegalCommentManageService {

	@PostMapping("/findList")
	@ApiOperation("违规记录分页列表")
	ResponseBase findList(IllegalCommentRecordEntity entity);

	@PostMapping("/detail")
	@ApiOperation("违规记录详情")
	ResponseBase detail(IllegalCommentRecordEntity entity);

	@PostMapping("/handle")
	@ApiOperation("后置处置：1通过 2删评 3禁言 4冻结")
	ResponseBase handle(IllegalCommentHandleQuery query, HttpServletRequest request);
}
