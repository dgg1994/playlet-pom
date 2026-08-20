package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.BatchDramaAssetReleaseQuery;
import com.playlet.internal.query.drama.DramaVideoUploadTokenQuery;
import com.playlet.internal.query.drama.UpdateDramaAssetQuery;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
	 * 登记/同步剧集（纯 JSON）：支持批量；历史集 assetId+setNum，新上传 setNum+key。
	 */
	@PostMapping("/release")
	@ApiOperation(value = "登记/同步剧集", notes = "body: {dramaId, episodes:[{assetId?, setNum, key?, videoName?, remarkInfo?}]}。"
			+ "历史集：assetId+setNum，key 可不传，仅更新集序/备注，不改 video_url。"
			+ "新集：setNum+key，校验七牛对象后登记并进审。"
			+ "驳回重传：assetId+key（可改 setNum）。"
			+ "同批 setNum 不可重复；集序批量调整时后端两阶段写入避免冲突。")
	ResponseBase addDrama(BatchDramaAssetReleaseQuery query);

	/**
	 * 修改已登记剧集（纯 JSON，不再接收 MultipartFile）。
	 */
	@PostMapping("/update")
	@ApiOperation(value = "修改剧集视频", notes = "body: {id, dramaId, setNum, key, videoName?, remarkInfo?}。"
			+ "仅允许修改已驳回剧集；可改集序并重传视频。"
			+ "修改成功后清空上一轮驳回/申诉痕迹，AI 审核默认通过，A/B 组恢复待审核，整集重新进入审核中。")
	ResponseBase updateDrama(UpdateDramaAssetQuery entity);

	/**
	 * 删除剧集视频。
	 */
	@GetMapping("/delete")
	ResponseBase delDrama(Long id);

}
