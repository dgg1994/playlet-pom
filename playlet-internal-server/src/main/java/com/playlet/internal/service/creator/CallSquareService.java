package com.playlet.internal.service.creator;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.creator.CreatorCallEntity;
import com.playlet.internal.entity.creator.CreatorCallSubmissionEntity;
import com.playlet.internal.query.creator.CallSquareListQuery;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * 征稿广场：征稿浏览、参与、发布。
 */
@RequestMapping("/api/talent/call")
@Api(value = "征稿广场", tags = "征稿广场")
public interface CallSquareService {

	@GetMapping("/list")
	@ApiOperation(value = "征稿列表", notes = "仅返回已上架征稿；支持状态/题材/截止日期筛选、5 种排序、分页、关键词")
	ResponseBase list(CallSquareListQuery query, HttpServletRequest request);

	@GetMapping("/detail")
	@ApiImplicitParam(name = "id", value = "征稿ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "征稿详情", notes = "已下架/不存在返回 404；含相关征稿与已投稿剧本")
	ResponseBase detail(@RequestParam("id") Integer id, HttpServletRequest request);

	@PostMapping("/join")
	@ApiImplicitParam(name = "id", value = "征稿ID", required = true, dataType = "Integer", paramType = "query")
	@ApiOperation(value = "参与征稿", notes = "需登录；重复参与幂等返回，已结束征稿不允许参与")
	ResponseBase join(@RequestParam("id") Integer id, HttpServletRequest request);

	@PostMapping("/submission/submit")
	@ApiOperation(value = "提交投稿", notes = "需登录；必填：征稿ID、剧本名称、剧本简介；同一征稿仅可投稿一次，投稿成功自动补记参与关系")
	ResponseBase submit(@RequestBody CreatorCallSubmissionEntity body, HttpServletRequest request);

	@PostMapping("/publish")
	@ApiOperation(value = "发布征稿", notes = "需登录；必填字段：标题、题材、总奖金、需求稿件数、截止日期、授权方式、卡片摘要")
	ResponseBase publish(@RequestBody CreatorCallEntity body, HttpServletRequest request);
}
