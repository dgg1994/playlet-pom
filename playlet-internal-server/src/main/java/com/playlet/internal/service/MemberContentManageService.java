package com.playlet.internal.service;

import com.playlet.internal.api.request.EpisodeUnlockConfigQuery;
import com.playlet.internal.api.request.MemberDramaConfigQuery;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端会员内容配置：网关 /china/admin/memberContentManage/**
 */
@RequestMapping("/memberContentManage")
@Api(value = "会员内容配置", tags = "会员内容配置")
public interface MemberContentManageService {

	@PostMapping("/setMemberDrama")
	@ApiOperation("设置是否会员短剧")
	ResponseBase setMemberDrama(MemberDramaConfigQuery query);

	@PostMapping("/updateUnlock")
	@ApiOperation("配置单集解锁类型与价格")
	ResponseBase updateUnlock(EpisodeUnlockConfigQuery query);

	@PostMapping("/batchUnlockFromSetNum")
	@ApiOperation("按剧从指定集序号起批量配置解锁")
	ResponseBase batchUnlockFromSetNum(EpisodeUnlockConfigQuery query);
}
