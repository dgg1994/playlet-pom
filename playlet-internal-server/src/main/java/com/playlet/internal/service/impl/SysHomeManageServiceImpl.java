package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.CreatorHomeFeedRespEntity;
import com.playlet.internal.api.response.CreatorHomeRankItemRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.enums.CreatorHomeRankTypeEnums;
import com.playlet.internal.service.SysHomeManageService;
import com.playlet.internal.utils.CreatorHomeFeedHelper;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端作家首页 Feed / 榜单（数据与创作者端同源，鉴权为管理端 token）。
 */
@Slf4j
@RestController
@CrossOrigin
public class SysHomeManageServiceImpl extends BaseApiService implements SysHomeManageService {

	@Autowired
	private CreatorHomeFeedHelper creatorHomeFeedHelper;

	@Override
	public ResponseBase feed(HttpServletRequest request) {
		Integer adminId = SysUserTokenUtil.resolveAdminId(request);
		if (adminId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		CreatorHomeFeedRespEntity resp = creatorHomeFeedHelper.buildFeed();
		int dramaSize = resp.getHotDramas() == null ? 0 : resp.getHotDramas().size();
		int tagSize = resp.getHotTags() == null ? 0 : resp.getHotTags().size();
		log.info("admin creator home feed adminId={} dramas={} tags={}", adminId, dramaSize, tagSize);
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase rank(@RequestParam(value = "type", required = false) Integer type,
			HttpServletRequest request) {
		Integer adminId = SysUserTokenUtil.resolveAdminId(request);
		if (adminId == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		CreatorHomeRankTypeEnums rankType = CreatorHomeRankTypeEnums.fromCode(type);
		if (rankType == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		List<CreatorHomeRankItemRespEntity> list = creatorHomeFeedHelper.buildRank(rankType);
		Map<String, Object> data = new HashMap<>(4);
		data.put("rankType", rankType.getCode());
		data.put("rankList", list);
		log.info("admin creator home rank adminId={} type={} size={}", adminId, rankType.getCode(), list.size());
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}
}
