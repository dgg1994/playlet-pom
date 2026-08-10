package com.playlet.oversea.utils;

import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.account.AppAccountDao;
import com.playlet.oversea.entity.account.AppAccountEntity;
import com.playlet.oversea.enums.UserStateEnums;
import com.playlet.oversea.filter.JWTAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求头 token 解析 C 端用户 uid。
 * <p>
 * 必须同时满足：JWT 合法、Redis 会话存在且与当前 token 一致、账号状态正常。
 * 登出 / 顶号 / 改密后删除 Redis，即可立即失效旧 token。
 */
@Slf4j
@Component
public class AppTokenUtil {

	private static AppAccountDao appAccountDao;
	private static RedisUtil redisUtil;

	@Autowired
	public void setAppAccountDao(AppAccountDao appAccountDao) {
		AppTokenUtil.appAccountDao = appAccountDao;
	}

	@Autowired
	public void setRedisUtil(RedisUtil redisUtil) {
		AppTokenUtil.redisUtil = redisUtil;
	}

	/**
	 * 解析登录用户 uid；未登录、token 无效或会话已吊销返回 null。
	 */
	public static Integer resolveUid(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		try {
			String header = request.getHeader(Constants.HEADER_AUTH);
			if (!StringUtils.hasText(header) || !header.startsWith(Constants.AUTH_HEADER_START_WITH)) {
				return null;
			}
			UsernamePasswordAuthenticationToken token = JWTAuthenticationFilter.getAuthentication(request);
			if (token == null || !StringUtils.hasText(token.getName())) {
				return null;
			}
			if (!isActiveSession(token.getName(), header)) {
				return null;
			}
			AppAccountEntity userEntity = appAccountDao.findByAccount(token.getName());
			if (userEntity != null && UserStateEnums.NORMAL.getIndex().equals(userEntity.getUserState())) {
				return userEntity.getId();
			}
		} catch (Exception e) {
			log.debug("resolveUid failed: {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 校验 Redis 中是否仍是该 token（登录写入、登出/改密删除）。
	 */
	public static boolean isActiveSession(String userAccount, String authHeader) {
		if (!StringUtils.hasText(userAccount) || !StringUtils.hasText(authHeader) || redisUtil == null) {
			return false;
		}
		Object cached = redisUtil.get(sessionKey(userAccount));
		return cached != null && authHeader.equals(String.valueOf(cached));
	}

	/**
	 * 吊销账号会话（踢下线）。会同时清理 userAccount / userEmail 两种历史 key。
	 */
	public static void invalidateAccountSessions(AppAccountEntity account) {
		if (account == null || redisUtil == null) {
			return;
		}
		if (StringUtils.hasText(account.getUserAccount())) {
			redisUtil.del(sessionKey(account.getUserAccount()));
		}
		if (StringUtils.hasText(account.getUserEmail())
				&& !account.getUserEmail().equals(account.getUserAccount())) {
			redisUtil.del(sessionKey(account.getUserEmail()));
		}
	}

	/**
	 * 按登录账号吊销会话。
	 */
	public static void invalidateSessionByAccount(String userAccount) {
		if (!StringUtils.hasText(userAccount) || redisUtil == null) {
			return;
		}
		redisUtil.del(sessionKey(userAccount));
	}

	public static String sessionKey(String userAccount) {
		return Constants.APP_PACKAGE_NAME + userAccount;
	}
}
