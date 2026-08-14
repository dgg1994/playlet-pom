package com.playlet.internal.utils;

import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.filter.JWTAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 作家端 token 解析（与 C 端 APP_PACKAGE_NAME 会话隔离）。
 */
@Slf4j
@Component
public class CreatorTokenUtil {

	private static CreatorAccountDao creatorAccountDao;
	private static RedisUtil redisUtil;

	@Autowired
	public void setCreatorAccountDao(CreatorAccountDao creatorAccountDao) {
		CreatorTokenUtil.creatorAccountDao = creatorAccountDao;
	}

	@Autowired
	public void setRedisUtil(RedisUtil redisUtil) {
		CreatorTokenUtil.redisUtil = redisUtil;
	}

	/**
	 * 解析当前登录作家 id；未登录或会话已吊销返回 null。
	 */
	public static Integer resolveCreatorId(HttpServletRequest request) {
		CreatorAccountEntity account = resolveAccount(request);
		return account == null ? null : account.getId();
	}

	/**
	 * 解析当前登录作家账号。
	 */
	public static CreatorAccountEntity resolveAccount(HttpServletRequest request) {
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
			String email = stripSubject(token.getName());
			if (!isActiveSession(email, header)) {
				return null;
			}
			CreatorAccountEntity account = creatorAccountDao.findByAccount(email);
			if (account != null && UserStateEnums.NORMAL.getIndex().equals(account.getUserState())) {
				return account;
			}
		} catch (Exception e) {
			log.debug("resolveCreator failed: {}", e.getMessage());
		}
		return null;
	}

	public static boolean isActiveSession(String email, String authHeader) {
		if (!StringUtils.hasText(email) || !StringUtils.hasText(authHeader) || redisUtil == null) {
			return false;
		}
		Object cached = redisUtil.get(sessionKey(email));
		return cached != null && authHeader.equals(String.valueOf(cached));
	}

	public static void invalidateSession(String email) {
		if (!StringUtils.hasText(email) || redisUtil == null) {
			return;
		}
		redisUtil.del(sessionKey(email));
	}

	public static String sessionKey(String email) {
		return RedisKeyConstants.CREATOR_TOKEN_KEY + email;
	}

	public static String jwtSubject(String email) {
		return CreatorConstants.JWT_SUBJECT_PREFIX + email;
	}

	private static String stripSubject(String subject) {
		if (subject != null && subject.startsWith(CreatorConstants.JWT_SUBJECT_PREFIX)) {
			return subject.substring(CreatorConstants.JWT_SUBJECT_PREFIX.length());
		}
		return subject;
	}
}
