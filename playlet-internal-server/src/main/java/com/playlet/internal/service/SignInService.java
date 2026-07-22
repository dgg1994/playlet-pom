package com.playlet.internal.service;

import com.playlet.internal.api.response.SignInHomeSummaryEntity;
import com.playlet.internal.base.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * C 端连续签到接口。
 * <p>
 * 路径前缀：{@code /api/welfare}（经网关 {@code /api/**} 路由）。
 * 补签消耗「补签卡」；福利首页通过 {@link #buildHomeSummary(Integer)} 组装摘要。
 * </p>
 *
 * @author GeminiSun
 */
@RequestMapping("/api/welfare")
@Api(value = "签到", tags = "签到")
public interface SignInService {

	/**
	 * 今日签到：写明细、发阶梯奖励、更新连续天数；成功返回最新签到摘要。
	 */
	@GetMapping("/signIn")
	@ApiOperation(value = "每日签到", notes = "今日签到发奖；成功返回签到摘要；需登录")
	ResponseBase signIn(HttpServletRequest request);

	/**
	 * 补签：消耗补签卡，按该日连续档位发奖并重算 streak。
	 *
	 * @param bizDate 补签日 yyyy-MM-dd，不可为今天或未来
	 */
	@GetMapping("/signIn/makeup")
	@ApiImplicitParam(name = "bizDate", value = "补签日期 yyyy-MM-dd", required = true, dataType = "string", paramType = "query")
	@ApiOperation(value = "补签", notes = "消耗补签卡补近N天内未签日期；成功返回签到摘要；需登录")
	ResponseBase makeup(@RequestParam String bizDate, HttpServletRequest request);

	/**
	 * 月历：每日 signed / makeup / today / empty，以及补签卡余额与限购信息。
	 *
	 * @param yearMonth 可选，默认当月，格式 yyyy-MM
	 */
	@GetMapping("/signIn/calendar")
	@ApiImplicitParam(name = "yearMonth", value = "年月 yyyy-MM，默认当月", required = false, dataType = "string", paramType = "query")
	@ApiOperation(value = "签到月历", notes = "需登录")
	ResponseBase calendar(@RequestParam(required = false) String yearMonth, HttpServletRequest request);

	/**
	 * 购买补签卡（扣金币），受每月限购约束。
	 *
	 * @param count 购买张数，默认 1
	 */
	@PostMapping("/signIn/makeupCard/buy")
	@ApiImplicitParam(name = "count", value = "购买张数", required = false, dataType = "int", paramType = "query")
	@ApiOperation(value = "购买补签卡", notes = "扣金币增加补签卡；需登录")
	ResponseBase buyMakeupCard(@RequestParam(required = false, defaultValue = "1") Integer count,
			HttpServletRequest request);

	/**
	 * 构建福利首页签到摘要（内部调用，无 HTTP 映射）。
	 * 全局配置未启用或无奖励阶梯时返回 {@code null}。
	 */
	SignInHomeSummaryEntity buildHomeSummary(Integer uid);

	/**
	 * 发放补签卡（内部：福袋/活动等），bizId 用于流水幂等时可扩展。
	 */
	boolean grantMakeupCards(Integer uid, int count, String remark);
}
