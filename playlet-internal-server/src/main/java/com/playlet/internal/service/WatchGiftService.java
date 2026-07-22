package com.playlet.internal.service;

import com.playlet.internal.api.response.WatchGiftHomeSummaryEntity;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端观影礼（时长阶梯）：摘要拼进福利 home；时长由 theater view/report 的 deltaSeconds 累计。
 */
@RequestMapping("/api/welfare")
@Api(value = "观影礼", tags = "观影礼")
public interface WatchGiftService {

	@PostMapping("/watchGift/claim")
	@ApiImplicitParam(name = "gearIndex", value = "档位序号", required = true, dataType = "int", paramType = "query")
	@ApiOperation(value = "领取观影礼单档奖励", notes = "需登录")
	ResponseBase claim(@RequestParam Integer gearIndex, HttpServletRequest request);

	@PostMapping("/watchGift/claimAll")
	@ApiOperation(value = "领取全部可领观影礼档位", notes = "需登录")
	ResponseBase claimAll(HttpServletRequest request);

	/** 福利 home 摘要；未开启返回 null */
	WatchGiftHomeSummaryEntity buildHomeSummary(Integer uid);

	/**
	 * 累计有效观看秒数（由 reportWatch 调用）。
	 *
	 * @param deltaSeconds 本次增量秒数
	 * @param extInfo      可选扩展 JSON
	 */
	void addWatchSeconds(Integer uid, int deltaSeconds, String extInfo);
}
