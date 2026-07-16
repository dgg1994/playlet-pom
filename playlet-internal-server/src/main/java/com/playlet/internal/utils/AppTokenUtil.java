package com.playlet.internal.utils;

import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求头 token 解析 C 端用户 uid。
 */
@Slf4j
@Component
public class AppTokenUtil {

	private static AppAccountDao appAccountDao;

	@Autowired
	public void setAppAccountDao(AppAccountDao appAccountDao) {
		AppTokenUtil.appAccountDao = appAccountDao;
	}

	/**
	 * 解析登录用户 uid；未登录或 token 无效返回 null。
	 */
	public static String resolveUid(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String header = request.getHeader(Constants.HEADER_AUTH);
		if (StringUtils.isEmpty(header) || !header.startsWith(Constants.AUTH_HEADER_START_WITH)) {
			return null;
		}
		try {
			String subject = Jwts.parser()
					.setSigningKey(Constants.SIGNING_KEY)
					.parseClaimsJws(header.replace(Constants.AUTH_HEADER_START_WITH, ""))
					.getBody()
					.getSubject();
			if (StringUtils.isEmpty(subject)) {
				return null;
			}
			if (appAccountDao == null) {
				return subject;
			}
			AppAccountEntity byUid = appAccountDao.findByUid(subject);
			if (byUid != null) {
				return byUid.getUid();
			}
			AppAccountEntity byAccount = appAccountDao.findByAccount(subject);
			if (byAccount != null) {
				return byAccount.getUid();
			}
			return subject;
		} catch (Exception e) {
			log.debug("resolveUid skip: {}", e.getMessage());
			return null;
		}
	}
}
