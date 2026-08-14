package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaAssetQuery;
import com.playlet.internal.query.drama.DramaAssetAppealQuery;
import com.playlet.internal.query.drama.DramaAssetShelfQuery;
import com.playlet.internal.query.drama.DramaVideoUploadTokenQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/dramaAsset")
@Api(value = "短剧剧集管理", tags = "短剧剧集管理")
public interface DramaAssetService {

	/**
	 * 获取剧集视频前端直传凭证（key 限定在 VD_{dramaId}/EP_{setNum}/）。
	 */
	@PostMapping("/uploadToken")
	@ApiOperation(value = "获取视频直传凭证", notes = "body: {dramaId, setNum, ext?}。"
			+ "返回 uploadToken/key/domain/expireSeconds/uploadUrl；前端直传七牛后再调 /release。")
	ResponseBase uploadToken(DramaVideoUploadTokenQuery query);
	
	/**
	 * 前端直传成功后登记剧集（纯 JSON，不再接收 MultipartFile）。
	 */
	@PostMapping("/release")
	@ApiOperation(value = "登记剧集视频", notes = "body: {dramaId, setNum, key, videoName?, remarkInfo?}。"
			+ "校验 key 前缀并确认对象存在后写入 drama_asset。"
			+ "同剧同集若已驳回则覆盖重传并重置 AI/A/B；审核中或已通过不可重复登记。"
			+ "上传成功后 AI 默认通过，进入 A/B 审核；过审后需再调 /shelf 上架。")
	ResponseBase addDrama(AddDramaAssetQuery entity);

	@PostMapping("/shelf")
	@ApiOperation(value = "集上架", notes = "集与所属剧均须审核通过；上架后若剧已过审则自动上架整剧。"
			+ "创作者调用时校验 belongUser。")
	ResponseBase shelf(DramaAssetShelfQuery query, HttpServletRequest request);

	@PostMapping("/unshelf")
	@ApiOperation(value = "集下架", notes = "下架本集；若该剧已无上架集则自动下架整剧。创作者调用时校验 belongUser。")
	ResponseBase unshelf(DramaAssetShelfQuery query, HttpServletRequest request);

	@PostMapping("/appeal")
	@ApiOperation(value = "集申诉", notes = "仅驳回后可发起；auditStatus=4 申诉中并重置 AI/A/B 进入再审；校验 belongUser。"
			+ "运营列表传 auditStatus=4 可见。")
	ResponseBase appeal(DramaAssetAppealQuery query, HttpServletRequest request);

	/**
	 * 删除剧集视频。
	 */
	@GetMapping("/delete")
	ResponseBase delDrama(Long id);

}
