package com.playlet.internal.service.impl;

import com.playlet.internal.api.request.UserRegisterEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.OauthLoginProperties;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppOauthAccountDao;
import com.playlet.internal.dao.account.UserFollowDao;
import com.playlet.internal.dao.drama.UserDramaLikeDao;
import com.playlet.internal.dao.template.EmailTemplateDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.AppOauthAccountEntity;
import com.playlet.internal.entity.template.EmailTemplateEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.filter.JWTAuthenticationFilter;
import com.playlet.internal.query.account.UpdatePwdEntity;
import com.playlet.internal.service.AppUserService;
import com.playlet.internal.utils.*;
import com.playlet.internal.utils.oidc.OidcIdTokenPayload;
import com.playlet.internal.utils.oidc.OidcTokenVerifier;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
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

	@Autowired
	private OauthLoginProperties oauthLoginProperties;

	@Autowired
	private AppOauthAccountDao appOauthAccountDao;

	@Autowired
	private UserFollowDao userFollowDao;

	@Autowired
	private UserDramaLikeDao UserDramaLikeDao;

	@Override
	public ResponseBase signUp(@RequestBody AppAccountEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getUserEmail())
				|| StringUtils.isEmpty(entity.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 校验邮箱验证码
		if (!verifyCode(EMAIL_CODE_PREFIX + entity.getUserEmail(), entity.getEmailCode())) {
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
		account.setUserPassword(DigestUtils.md5DigestAsHex((entity.getUserPassword()).getBytes()));
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
		return setResultSuccess();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ResponseBase login(@RequestBody AppAccountEntity entity, HttpServletRequest req) {
		try {
			if(entity.getLoginType() == null) {
				return setResultError(I18nUtil.getMessage("user.account_error"));
			}
			AppAccountEntity appUserEntity = null;
			if(PublicEnums.ONE.getIndex().equals(entity.getLoginType())) {//邮箱登录
				appUserEntity = appAccountDao.findByEmail(entity.getUserAccount());
			}else if(PublicEnums.TOW.getIndex().equals(entity.getLoginType())) {//手机号登录
				appUserEntity = appAccountDao.findByTel(entity.getUserAccount(),entity.getMobilePrefix());
			}
			if (appUserEntity == null) {
				return setResultError(I18nUtil.getMessage("user.account_error"));
			}
			if (!UserStateEnums.NORMAL.getIndex().equals(appUserEntity.getUserState())) {
				return setResultError(I18nUtil.getMessage("user.account_null"));
			}
			if (entity.getUserPassword() != null
					&& DigestUtils.md5DigestAsHex((entity.getUserPassword()).getBytes())
					.equals(appUserEntity.getUserPassword())) {
				String token = Jwts.builder()
						// 设置主题
						.setSubject(appUserEntity.getUserAccount())
						// 设置到期时间
						.setExpiration(new Date(System.currentTimeMillis() + Constants.USER_JWT_EXPIRE_TIME))
						// 选择 加密算法和私钥
						.signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY).compact();
				redisUtil.set(Constants.APP_PACKAGE_NAME + appUserEntity.getUserAccount(),
						Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME);
				//保存更换jpush appUserEntity
				if(entity.getRegistrationId() != null) {
					appUserEntity.setRegistrationId(entity.getRegistrationId());
					appAccountDao.updateById(appUserEntity);
				}
				return setResultSuccess(Constants.AUTH_HEADER_START_WITH + token, I18nUtil.getMessage("base_success"));
			} else {
				return setResultError(I18nUtil.getMessage("user.password_error"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase oneClickLogin(@RequestBody AppAccountEntity entity, HttpServletRequest request) {
		Integer type = entity.getType();
		if (type == null || entity.getIdToken() == null || entity.getIdToken().trim().isEmpty()) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		boolean isApple = LoginTypeEnums.APPLE.getIndex().equals(type);
		if (!isApple && !LoginTypeEnums.GOOGLE.getIndex().equals(type)) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		try {
			if (isApple) {
				List<String> appleAud = oauthLoginProperties.resolveAppleClientIds();
				if (appleAud.isEmpty()) {
					log.warn("oauth.apple 未配置，无法校验 id_token");
					return setResultError("服务器未配置苹果登录");
				}
				OidcIdTokenPayload payload = OidcTokenVerifier.verifyAppleIdToken(entity.getIdToken(), appleAud);
				if ((payload.getEmail() == null || payload.getEmail().trim().isEmpty())
						&& entity.getUserEmail() != null && !entity.getUserEmail().trim().isEmpty()) {
					payload = new OidcIdTokenPayload(payload.getSub(), entity.getUserEmail().trim(), payload.getEmailVerified());
				}
				return thirdPartyLogin("apple", payload, entity, request);
			}
			List<String> googleClients = oauthLoginProperties.getGoogle().getClientIds();
			if (googleClients == null || googleClients.isEmpty()) {
				log.warn("oauth.google.clientIds 未配置，无法校验 id_token");
				return setResultError("服务器未配置谷歌登录");
			}
			OidcIdTokenPayload payload = OidcTokenVerifier.verifyGoogleIdToken(entity.getIdToken(), googleClients);
			return thirdPartyLogin("google", payload, entity, request);
		} catch (Exception e) {
			log.warn("oneClickLogin 失败 type={}", type, e);
			return setResultError(isApple ? "苹果登录失败" : "谷歌登录失败");
		}
	}

	@SuppressWarnings("deprecation")
	private ResponseBase thirdPartyLogin(String provider, OidcIdTokenPayload payload, AppAccountEntity entity, HttpServletRequest request) {
		if (payload == null || payload.getSub() == null || payload.getSub().trim().isEmpty()) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}

		AppOauthAccountEntity binding = appOauthAccountDao.findByProviderAndSub(provider, payload.getSub());
		AppAccountEntity account = null;

		if (binding != null && binding.getUid() != null) {
			account = appAccountDao.findByUid(binding.getUid());
		}

		if (account == null) {
			String email = payload.getEmail();
			if (email != null && !email.trim().isEmpty()) {
				if ("google".equals(provider) && Boolean.FALSE.equals(payload.getEmailVerified())) {
					return setResultError("谷歌邮箱未验证");
				}
				account = appAccountDao.findByEmail(email.trim());
			}
		}

		if (account == null) {
			String email = payload.getEmail() == null ? null : payload.getEmail().trim();
			if (email == null || email.isEmpty()) {
				return setResultError("缺少邮箱，无法创建账号");
			}
			if ("google".equals(provider) && Boolean.FALSE.equals(payload.getEmailVerified())) {
				return setResultError("谷歌邮箱未验证");
			}
			UserRegisterEntity apiEntity = new UserRegisterEntity();
			apiEntity.setEmail(email);
			AppAccountEntity newEntity = new AppAccountEntity();
			newEntity.setUserEmail(email);
			newEntity.setUserAccount(email);
			newEntity.setUserPassword("");
			newEntity.setUserState(UserStateEnums.NORMAL.getIndex());
			this.addAccount(newEntity, RegisterSourceEnums.ONE_CLICK_LOGIN.getIndex());

			if (entity.getEnterInvitationCode() != null && !entity.getEnterInvitationCode().isEmpty()) {
				AppAccountEntity inviter = appAccountDao.findByIncitationCode(entity.getEnterInvitationCode());
				if (inviter != null) {
					this.inviteRelation(newEntity, inviter);
				}
			}

			account = appAccountDao.findByEmail(email);
		}

		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		if (!UserStateEnums.NORMAL.getIndex().equals(account.getUserState())) {
			return setResultError(I18nUtil.getMessage("user.account_null"));
		}

		if (binding == null) {
			AppOauthAccountEntity row = new AppOauthAccountEntity();
			row.setProvider(provider);
			row.setProviderSub(payload.getSub());
			row.setUid(account.getUid());
			row.setEmail(payload.getEmail());
			try {
				GenericityUtil.setDate(row);
				appOauthAccountDao.insert(row);
			} catch (Exception ex) {
				log.debug("oauth 绑定已存在或并发: provider={} sub={}", provider, payload.getSub());
			}
		}

		String token = Jwts.builder()
				.setSubject(account.getUserAccount())
				.setExpiration(new Date(System.currentTimeMillis() + Constants.USER_JWT_EXPIRE_TIME))
				.signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY).compact();
		redisUtil.set(Constants.APP_PACKAGE_NAME + account.getUserAccount(),
				Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME);

		if (entity.getRegistrationId() != null) {
			account.setRegistrationId(entity.getRegistrationId());
			appAccountDao.updateById(account);
		}

		return setResultSuccess(Constants.AUTH_HEADER_START_WITH + token, I18nUtil.getMessage("base_success"));
	}

	/**
	 * @category 处理绑定邀请关系
	 * @param entity 被邀请人
	 * @param accountEntity 邀请人
	 */
	public void inviteRelation(AppAccountEntity entity, AppAccountEntity accountEntity) {
		try {
			//@todo 待定处理
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	//创建各账户
	public void addAccount(AppAccountEntity entity,Integer source) {
		try {
			entity.setUserAccount(entity.getUserEmail());
			entity.setUserPassword(DigestUtils.md5DigestAsHex((entity.getUserPassword()).getBytes()));
			entity.setUserState(UserStateEnums.NORMAL.getIndex());
			//添加注册来源 1：一键注册用户 2:正常注册用户
			entity.setRegisterSource(source);
			entity.setInvitationCode(RandomSuffixInviteCodeUtil.generateUniqueCode(Long.parseLong(entity.getId().toString()), 4, 6));
			GenericityUtil.setDate(entity);
			appAccountDao.insert(entity);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase findToken(HttpServletRequest request) {
		try {
			String header = request.getHeader(Constants.HEADER_AUTH);
			if (header == null) {
				return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
			}
			UsernamePasswordAuthenticationToken userData = JWTAuthenticationFilter.getAuthentication(request);
			if (userData == null) {
				return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
			} else {
				String key = userData.getName();
				String redisKey = Constants.APP_PACKAGE_NAME + key;
				if (redisUtil.get(redisKey) == null || redisUtil.get(redisKey).toString().length() < 1) {
					return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
				}
				if (!redisUtil.get(redisKey).equals(header)) {
					return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
				}
			}
			String username = userData.getName();
			AppAccountEntity entity = appAccountDao.findByAccount(username);
			entity.setUserPassword(null);
			entity.setPayPassword(null);
			entity.setGoogleSecretkey(null);
			entity.setFollowCount(userFollowDao.countFollowing(entity.getUid()));
			entity.setFansCount(userFollowDao.countFans(entity.getUid()));
			entity.setLikeCount(UserDramaLikeDao.countLike(entity.getUid()));
			return setResultSuccess(entity);
		} catch (Exception e) {
			e.printStackTrace();
			return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
		}
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
				redisUtil.set(EMAIL_CODE_PREFIX + userEmail, code, Constants.CODE_EXPIRE_TIME);
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
	public ResponseBase updatePwd(@RequestBody UpdatePwdEntity entity, HttpServletRequest request) {
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
		try {
			String code = OrderCodeFactory.getRandomStr(6);
			ResponseBase base = SmsUtil.sendVerificationCode(tel, code);
			if(Constants.HTTP_RES_CODE_200.equals(base.getCode())) {
				redisUtil.set(tel, code, Constants.CODE_EXPIRE_TIME);
			}
			return base;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase checkTelCode(String tel, String telCode) {
		try {
			String temp = null;
			if (redisUtil.get(tel) != null) {
				temp = redisUtil.get(tel).toString();
			}
			if (temp != null && telCode.equals(temp)) {
				return setResultSuccess();
			} else {
				return setResultError(I18nUtil.getMessage("verification_error"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	// ==================== 私有工具方法 ====================

	/**
	 * 生成登录结果：签发 token 并写入 redis，返回脱敏后的账户信息
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("deprecation")
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
	@SuppressWarnings("deprecation")
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
	@SuppressWarnings("unused")
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
