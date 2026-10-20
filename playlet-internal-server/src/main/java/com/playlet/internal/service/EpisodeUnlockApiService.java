package com.playlet.internal.service;

import com.playlet.internal.api.request.EpisodeUnlockAdReqEntity;
import com.playlet.internal.api.request.EpisodeUnlockCashCreateReqEntity;
import com.playlet.internal.api.request.EpisodeUnlockCashNotifyReqEntity;
import com.playlet.internal.api.request.EpisodeUnlockCoinReqEntity;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端剧集播放策略与解锁：网关 /entrance/api/episode/**
 */
@RequestMapping("/api/episode")
@Api(value = "剧集解锁", tags = "剧集解锁")
public interface EpisodeUnlockApiService {

	@GetMapping("/playAccess")
	@ApiOperation(value = "播放策略", notes = "可不登录。返回 canPlay / blockReason / 价格；"
			+ "会员短剧需 VIP；广告集 VIP 免广告；付费集需金币或现金解锁")
	ResponseBase playAccess(@RequestParam("assetId") Integer assetId, HttpServletRequest request);

	@PostMapping("/unlock/coin")
	@ApiOperation(value = "金币解锁", notes = "需登录；仅付费集（unlock_type=2）且非会员短剧")
	ResponseBase unlockByCoin(HttpServletRequest request, EpisodeUnlockCoinReqEntity req);

	@PostMapping("/unlock/cash/create")
	@ApiOperation(value = "现金解锁下单", notes = "需登录；生成待支付单，支付成功后调 cash/notify")
	ResponseBase createCashOrder(HttpServletRequest request, EpisodeUnlockCashCreateReqEntity req);

	@PostMapping("/unlock/cash/notify")
	@ApiOperation(value = "现金解锁支付确认", notes = "幂等写入解锁记录；正式环境应加签验签")
	ResponseBase cashNotify(EpisodeUnlockCashNotifyReqEntity req);

	@PostMapping("/unlock/ad")
	@ApiOperation(value = "广告解锁", notes = "需登录；激励广告完成后上报 transId，校验幂等后解锁")
	ResponseBase unlockByAd(HttpServletRequest request, EpisodeUnlockAdReqEntity req);
}
