package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.template.EmailTemplateDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.system.EmailTemplateEntity;
import com.playlet.internal.enums.MessageEnums;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.query.pub.UpdatePwdEntity;
import com.playlet.internal.service.AppUserService;
import com.playlet.internal.utils.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.playlet.internal.constants.Constants.APP_PACKAGE_NAME;

@Slf4j
@RestController
@Transactional
@CrossOrigin
public class AppUserServiceImpl extends BaseApiService implements AppUserService {

	/** app 用户 token 在 redis 中的 key 前缀 */
	private static final String APP_TOKEN_PREFIX = APP_PACKAGE_NAME + "app:token:";
	/** 邮箱验证码 redis key 前缀 */
	private static final String EMAIL_CODE_PREFIX = APP_PACKAGE_NAME + "app:emailCode:";
	/** 手机验证码 redis key 前缀 */
	private static final String TEL_CODE_PREFIX = APP_PACKAGE_NAME + "app:telCode:";

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private AppAccountDao appAccountDao;

	@Autowired
	private EmailTemplateDao emailTemplateDao;

	@Override
	public ResponseBase signUp(@RequestBody AppAccountEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getUserEmail())
				|| StringUtils.isEmpty(entity.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 校验邮箱验证码
		if (!verifyCode(entity.getUserEmail(), entity.getEmailCode())) {
			return setResultError("验证码错误或已过期");
		}
		// 邮箱唯一性校验
		if (appAccountDao.findByEmail(entity.getUserEmail()) != null) {
			return setResultError(I18nUtil.getMessage("user.account_exist"));
		}
		AppAccountEntity account = new AppAccountEntity();
		account.setUid(generateUid());
		account.setUserAccount(StringUtils.isNotEmpty(entity.getUserAccount()) ? entity.getUserAccount()
				: entity.getUserEmail());
		account.setUserEmail(entity.getUserEmail());
		account.setUserPassword(MD5Util.digest(entity.getUserPassword()));
		account.setMobileNumber(entity.getMobileNumber());
		account.setMobilePrefix(entity.getMobilePrefix());
		account.setInvitationCode(generateInvitationCode());
		account.setRegisterSource(2);
		account.setRegistrationId(entity.getRegistrationId());
		account.setUserState(UserStateEnums.NORMAL.getIndex());
		account.setSetTime(new Date());
		account.setGmtModified(new Date());
		appAccountDao.insert(account);
		// 清除验证码
		redisUtil.del(EMAIL_CODE_PREFIX + entity.getUserEmail());
		return buildLoginResult(account);
	}

	@Override
	public ResponseBase login(@RequestBody AppAccountEntity entity, HttpServletRequest req) {
		if (entity == null || StringUtils.isEmpty(entity.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppAccountEntity account = null;
		if (StringUtils.isNotEmpty(entity.getUserEmail())) {
			account = appAccountDao.findByEmail(entity.getUserEmail());
		} else if (StringUtils.isNotEmpty(entity.getUserAccount())) {
			account = appAccountDao.findByAccount(entity.getUserAccount());
		} else if (StringUtils.isNotEmpty(entity.getMobileNumber())) {
			account = appAccountDao.findByMobile(entity.getMobileNumber());
		}
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		if (UserStateEnums.LOGOUT.getIndex().equals(account.getUserState())) {
			return setResultError(I18nUtil.getMessage("user.account_null"));
		}
		if (UserStateEnums.DISABLE.getIndex().equals(account.getUserState())) {
			return setResultError("账户已被禁用");
		}
		if (!MD5Util.digest(entity.getUserPassword()).equals(account.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("user.password_error"));
		}
		// 更新推送 id
		if (StringUtils.isNotEmpty(entity.getRegistrationId())) {
			account.setRegistrationId(entity.getRegistrationId());
			account.setGmtModified(new Date());
			appAccountDao.updateById(account);
		}
		return buildLoginResult(account);
	}

	@Override
	public ResponseBase oneClickLogin(@RequestBody AppAccountEntity entity, HttpServletRequest req) {
		if (entity == null || StringUtils.isEmpty(entity.getIdToken()) || entity.getType() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// TODO 校验 Apple/Google 的 idToken 合法性并解析出三方唯一 id，此处暂以 idToken 作为三方 uid
		String thirdUid = entity.getIdToken();
		AppAccountEntity account = appAccountDao.findByUid(thirdUid);
		if (account == null) {
			// 无用户则自动注册
			account = new AppAccountEntity();
			account.setUid(thirdUid);
			account.setUserAccount(entity.getType() + "_" + generateInvitationCode());
			account.setUserEmail(entity.getUserEmail());
			account.setInvitationCode(generateInvitationCode());
			account.setRegisterSource(1);
			account.setRegistrationId(entity.getRegistrationId());
			account.setUserState(UserStateEnums.NORMAL.getIndex());
			account.setSetTime(new Date());
			account.setGmtModified(new Date());
			appAccountDao.insert(account);
		} else if (UserStateEnums.DISABLE.getIndex().equals(account.getUserState())) {
			return setResultError("账户已被禁用");
		}
		return buildLoginResult(account);
	}

	@Override
	public ResponseBase findToken(HttpServletRequest request) {
		String uid = parseUidFromRequest(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		account.setUserPassword(null);
		account.setPayPassword(null);
		account.setGoogleSecretkey(null);
		return setResultSuccess(account, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase sendEmailCode(String userEmail) {
		try {
			String language = LanguageContext.getLanguage();
			EmailTemplateEntity templateEntity = emailTemplateDao.findByNum(MessageEnums.SEND_CODE_ZH.getIndex(), language);
			if (templateEntity != null && templateEntity.getTemplateContent() != null
					&& templateEntity.getTemplateContent().length() > 0) {
				String code = OrderCodeFactory.getRandomStr(6);
				//添加动态数据
				String htmlContent = MessageFormatUtils.format(
						templateEntity.getTemplateContent(),
						userEmail,
						code);
				//组装html内容
				String html = MessageFormatUtils.saveHtml(htmlContent, language);
				EmailUtil.sendEmail(userEmail, templateEntity.getTemplateSubject(), html);
				redisUtil.set(APP_PACKAGE_NAME + userEmail, code, Constants.CODE_EXPIRE_TIME);
				return setResultSuccess(I18nUtil.getMessage("send_success"));
			} else {
				return setResultError(I18nUtil.getMessage("Template_null"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase checkEmailCode(String userEmail, String emailCode) {
		if (verifyCode(EMAIL_CODE_PREFIX + userEmail, emailCode)) {
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		return setResultError("验证码错误或已过期");
	}

	@Override
	public ResponseBase updaePwd(@RequestBody UpdatePwdEntity entity, HttpServletRequest request) {
		String uid = parseUidFromRequest(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		if (entity == null || StringUtils.isEmpty(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		// 校验原密码
		if (StringUtils.isNotEmpty(account.getUserPassword())
				&& !MD5Util.digest(StringUtils.trim(entity.getFormerPassword())).equals(account.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("old_password_error"));
		}
		account.setUserPassword(MD5Util.digest(entity.getNewPassword()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase logout(String uid, HttpServletRequest request) {
		if (StringUtils.isEmpty(uid)) {
			uid = parseUidFromRequest(request);
		}
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		account.setUserState(UserStateEnums.LOGOUT.getIndex());
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		redisUtil.del(APP_TOKEN_PREFIX + uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase signOut(HttpServletRequest request) {
		String uid = parseUidFromRequest(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		redisUtil.del(APP_TOKEN_PREFIX + uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase forgetPasswrod(@RequestBody UpdatePwdEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getEmail())
				|| StringUtils.isEmpty(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!verifyCode(EMAIL_CODE_PREFIX + entity.getEmail(), entity.getEmailCode())) {
			return setResultError("验证码错误或已过期");
		}
		AppAccountEntity account = appAccountDao.findByEmail(entity.getEmail());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		account.setUserPassword(MD5Util.digest(entity.getNewPassword()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		redisUtil.del(EMAIL_CODE_PREFIX + entity.getEmail());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase bindingTel(@RequestBody AppAccountEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getUid())
				|| StringUtils.isEmpty(entity.getMobileNumber())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!verifyCode(TEL_CODE_PREFIX + entity.getMobileNumber(), entity.getTelCode())) {
			return setResultError("验证码错误或已过期");
		}
		AppAccountEntity account = appAccountDao.findByUid(entity.getUid());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		// 手机号唯一性校验
		AppAccountEntity exist = appAccountDao.findByMobile(entity.getMobileNumber());
		if (exist != null && !exist.getUid().equals(entity.getUid())) {
			return setResultError(I18nUtil.getMessage("base_info_exist"));
		}
		account.setMobileNumber(entity.getMobileNumber());
		account.setMobilePrefix(entity.getMobilePrefix());
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		redisUtil.del(TEL_CODE_PREFIX + entity.getMobileNumber());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase sendTelCode(String tel) {
		if (StringUtils.isEmpty(tel)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String code = generateCode();
		redisUtil.set(TEL_CODE_PREFIX + tel, code, Constants.CODE_EXPIRE_TIME);
		// TODO 接入短信服务发送验证码，当前仅缓存并打印
		log.info("send tel code to {} : {}", tel, code);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase checkTelCode(String tel, String telCode) {
		if (verifyCode(TEL_CODE_PREFIX + tel, telCode)) {
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		return setResultError("验证码错误或已过期");
	}

	@Override
	public ResponseBase findList(@RequestBody AppAccountEntity entity) {
		if (entity == null) {
			entity = new AppAccountEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<AppAccountEntity> list = appAccountDao.findList(entity);
		if (list != null) {
			for (AppAccountEntity item : list) {
				item.setUserPassword(null);
				item.setPayPassword(null);
				item.setGoogleSecretkey(null);
			}
		}
		PageInfo<AppAccountEntity> pageInfo = new PageInfo<>(list);
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	// ==================== 私有工具方法 ====================

	/**
	 * 生成登录结果：签发 token 并写入 redis，返回脱敏后的账户信息
	 */
	private ResponseBase buildLoginResult(AppAccountEntity account) {
		String token = createToken(account.getUid());
		redisUtil.set(APP_TOKEN_PREFIX + account.getUid(), token, Constants.REDIS_EXPIRE_TIME / 1000);
		account.setUserPassword(null);
		account.setPayPassword(null);
		account.setGoogleSecretkey(null);
		com.alibaba.fastjson.JSONObject data = new com.alibaba.fastjson.JSONObject();
		data.put("token", Constants.AUTH_HEADER_START_WITH + token);
		data.put("user", account);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 使用 uid 作为 subject 签发 JWT
	 */
	private String createToken(String uid) {
		return Jwts.builder()
				.setSubject(uid)
				.setExpiration(new Date(System.currentTimeMillis() + Constants.REDIS_EXPIRE_TIME))
				.signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY)
				.compact();
	}

	/**
	 * 从请求头解析 token 得到 uid，并与 redis 中的 token 双重校验
	 */
	private String parseUidFromRequest(HttpServletRequest request) {
		String header = request.getHeader(Constants.HEADER_AUTH);
		if (StringUtils.isEmpty(header)) {
			return null;
		}
		String token = header.replace(Constants.AUTH_HEADER_START_WITH, "");
		try {
			String uid = Jwts.parser()
					.setSigningKey(Constants.SIGNING_KEY)
					.parseClaimsJws(token)
					.getBody()
					.getSubject();
			if (StringUtils.isEmpty(uid)) {
				return null;
			}
			Object cache = redisUtil.get(APP_TOKEN_PREFIX + uid);
			if (cache == null || !cache.toString().equals(token)) {
				return null;
			}
			return uid;
		} catch (ExpiredJwtException e) {
			log.warn("app token expired: {}", e.getMessage());
			return null;
		} catch (Exception e) {
			log.warn("app token parse error: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * 校验验证码
	 */
	private boolean verifyCode(String key, String code) {
		if (StringUtils.isEmpty(code)) {
			return false;
		}
		Object cache = redisUtil.get(key);
		return cache != null && cache.toString().equals(code);
	}

	/**
	 * 生成 6 位数字验证码
	 */
	private String generateCode() {
		return String.format("%06d", new Random().nextInt(1000000));
	}

	/**
	 * 生成唯一 uid
	 */
	private String generateUid() {
		return java.util.UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 生成 8 位邀请码
	 */
	private String generateInvitationCode() {
		String chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 8; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		return sb.toString();
	}
}
