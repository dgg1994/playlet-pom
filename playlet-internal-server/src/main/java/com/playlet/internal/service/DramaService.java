package com.playlet.internal.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.query.drama.AddDramaQuery;
import com.playlet.internal.query.drama.QueryDramaQuery;
import com.playlet.internal.query.drama.UpdateDramaQuery;

import io.swagger.annotations.Api;

@RequestMapping("/drama")
@Api(value = "短剧管理", tags = "短剧管理")
public interface DramaService {
	
	/**
	 * @category 短剧基础信息发布
	 * @param entity
	 * @param file
	 * @return
	 */
	@PostMapping("/release")
	ResponseBase addDrama(AddDramaQuery entity,MultipartFile file);
	
	/**
	 * @category 短剧基础信息编辑修改
	 * @param entity
	 * @param file
	 * @return
	 */
	@PostMapping("/update")
	ResponseBase update(UpdateDramaQuery entity,MultipartFile file);
	
	/**
	 * @category 短剧基础信息列表
	 * @param entity
	 * @param file
	 * @return
	 */
	@PostMapping("/findList")
	ResponseBase findList(QueryDramaQuery entity);
	
	/**
	 * @category 删除短剧
	 * @param id
	 * @return
	 */
	@GetMapping("/delete")
	ResponseBase delete(Integer id);
	
	/**
	 * @category 修改审核状态
	 * @param id
	 * @return
	 */
	@GetMapping("/verifyStatus")
	ResponseBase verifyStatus(Integer id,Integer verifyStatus);
	
	/**
	 * @category 查询短剧上传的剧集视频
	 * @param id
	 * @return
	 */
	@GetMapping("/findVideo")
	ResponseBase findVideo(Integer id);

}
