package com.playlet.oversea.utils;

import com.playlet.oversea.dao.system.SysUserDao;
import com.playlet.oversea.entity.system.SysUserEntity;
import com.playlet.oversea.enums.UserStateEnums;
import com.playlet.oversea.filter.JWTAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 从请求头 token 解析管理端 sys_user.id。
 */
@Slf4j
@Component
public class SysUserTokenUtil {

	private static SysUserDao sysUserDao;

	@Autowired
	public void setSysUserDao(SysUserDao sysUserDao) {
		SysUserTokenUtil.sysUserDao = sysUserDao;
	}

	/**
	 * 解析当前登录管理员 id；token 无效或账号非正常返回 null。
	 */
	public static Integer resolveAdminId(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		try {
			UsernamePasswordAuthenticationToken token = JWTAuthenticationFilter.getAuthentication(request);
			if (token == null) {
				return null;
			}
			SysUserEntity admin = sysUserDao.findByAcctiveState(token.getName(), UserStateEnums.NORMAL.getIndex());
			return admin == null ? null : admin.getId();
		} catch (Exception e) {
			log.debug("resolveAdminId failed: {}", e.getMessage());
			return null;
		}
	}
}
