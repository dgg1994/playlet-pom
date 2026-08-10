package com.playlet.internal.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.base.JsonData;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.system.SysUserEntity;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.utils.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


/**
 * jwt登录拦截器 登录controller方法不用自己写，直接访问/login就行
 * 该类继承自UsernamePasswordAuthenticationFilter，重写了其中的2个方法
 * 验证用户名密码正确后，生成一个token，并将token返回给客户端 attemptAuthentication ：接收并解析用户凭证。
 * successfulAuthentication ：用户成功登录后，这个方法会被调用，我们在这个方法里生成token。
 *
 */
public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {
	
	@Autowired
	private GoogleAuthenticatorUtil googleAuthenticator;
	
	private AuthenticationManager authenticationManager;

	private RedisUtil redisUtil;

	private SysUserDao sysUserDao;
	
	private Boolean googleLimit;
	
	/** 谷歌验证码在 Redis 中缓存秒数 */
	private static final long GOOGLE_CODE_TTL_SEC = 120L;

	public JWTLoginFilter(AuthenticationManager authenticationManager, RedisUtil redisUtil,
			SysUserDao sysUserDao, Boolean googleLimit) {
		this.authenticationManager = authenticationManager;
		this.redisUtil = redisUtil;
		this.sysUserDao = sysUserDao;
	    this.googleLimit = googleLimit;
	}

	/**
	 * 处理登录请求，校验用户名和密码
	 * 
	 * @param req 请求
	 * @param res 响应
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {
		try {
			boolean isAllowed = true;
			if(isAllowed) {
				SysUserEntity sysUserLoginDTO = new ObjectMapper().readValue(req.getInputStream(), SysUserEntity.class);
				if (sysUserLoginDTO.getGoogleCode() == null) {
					throw new BadCredentialsException("请输入谷歌验证码");
				}
				if (sysUserLoginDTO.getUsername() == null || sysUserLoginDTO.getUsername().trim().isEmpty()) {
					throw new BadCredentialsException("请输入用户名");
				}
				// 按用户名隔离，避免并发登录串码
				String codeKey = googleCodeRedisKey(sysUserLoginDTO.getUsername().trim());
				redisUtil.set(codeKey, sysUserLoginDTO.getGoogleCode(), GOOGLE_CODE_TTL_SEC);
				return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
						sysUserLoginDTO.getUsername().trim(), sysUserLoginDTO.getPassword(), new ArrayList<>()));
			}else {
				throw new BadCredentialsException("对不起，你的IP地址没有访问权限");
			}
		} catch (IOException e) {
			throw new BaseException(e);
		}
	}

	/**
	 * 登录请求后，CustomAuthenticationProvider.authenticate身份验证通过会生成Authentication令牌
	 * 我们在这个方法里对令牌生成token，并设置在响应头中
	 * 
	 * @param req   请求
	 * @param res   响应
	 * @param chain 过滤链
	 * @param auth  身份认证
	 */
	@SuppressWarnings({ "static-access", "deprecation"})
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        String subject = auth.getName();
        String codeKey = googleCodeRedisKey(subject);
        Object codeObj = redisUtil.get(codeKey);
        redisUtil.del(codeKey);
        if (codeObj == null) {
            CustomUtils.sendJsonMessage(res, JsonData.Error("谷歌验证失败"));
            return;
        }
        String code = codeObj.toString();

        SysUserEntity entity = sysUserDao.findByAcctiveState(subject, UserStateEnums.NORMAL.getIndex());
        if (entity == null || entity.getGoogleSecretkey() == null || entity.getGoogleSecretkey().isEmpty()) {
            CustomUtils.sendJsonMessage(res, JsonData.Error("谷歌验证失败"));
            return;
        }
        boolean verified;
        try {
            if (Boolean.FALSE.equals(googleLimit)) {
                // 配置关闭校验时仍要求字段存在，但不验真（仅本地调试）
                verified = true;
            } else {
                verified = googleAuthenticator.verifyCode(entity.getGoogleSecretkey(), Integer.parseInt(code));
            }
        } catch (Exception e) {
            verified = false;
        }
        if (!verified) {
            CustomUtils.sendJsonMessage(res, JsonData.Error("谷歌验证失败"));
            return;
        }

        String token = Jwts.builder()
                .setSubject(subject)
                .setExpiration(new Date(System.currentTimeMillis() + Constants.REDIS_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY).compact();
        String sessionUser = subject.indexOf(',') == -1 ? subject : subject.substring(0, subject.indexOf(','));
        // REDIS_EXPIRE_TIME 为毫秒，RedisUtil.set 第三参为秒
        redisUtil.set(Constants.APP_PACKAGE_NAME + sessionUser,
                Constants.AUTH_HEADER_START_WITH + token,
                Constants.REDIS_EXPIRE_TIME / 1000);

        res.setHeader(Constants.HEADER_ACCESS, Constants.HEADER_AUTH);
        res.addHeader(Constants.HEADER_AUTH, Constants.AUTH_HEADER_START_WITH + token);
        CustomUtils.sendJsonMessage(res, JsonData.buildSuccess("登录成功"));
    }

	private static String googleCodeRedisKey(String username) {
		return RedisKeyConstants.GOOGLE_CODE_KEY + username;
	}

}
