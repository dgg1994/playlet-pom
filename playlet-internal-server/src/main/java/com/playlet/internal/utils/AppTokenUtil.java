package com.playlet.internal.utils;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.filter.JWTAuthenticationFilter;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
	public static Integer resolveUid(HttpServletRequest request) {
		UsernamePasswordAuthenticationToken token = JWTAuthenticationFilter.getAuthentication(request);
		if(token != null) {
			AppAccountEntity userEntity = appAccountDao.findByEmail(token.getName());
			if(userEntity != null && UserStateEnums.NORMAL.getIndex().equals(userEntity.getUserState()) ) {
				return userEntity.getId();
			}
		}
		return null;
		
	}
}
