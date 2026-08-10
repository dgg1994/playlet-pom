package com.playlet.oversea.service.impl;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.api.request.UserRegisterEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.OauthLoginProperties;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.constants.RedisKeyConstants;
import com.playlet.oversea.dao.account.AppAccountDao;
import com.playlet.oversea.dao.account.AppOauthAccountDao;
import com.playlet.oversea.dao.account.AppPushDeviceDao;
import com.playlet.oversea.dao.account.UserFollowDao;
import com.playlet.oversea.dao.drama.UserDramaLikeDao;
import com.playlet.oversea.dao.template.EmailTemplateDao;
import com.playlet.oversea.entity.account.AppAccountEntity;
import com.playlet.oversea.entity.account.AppOauthAccountEntity;
import com.playlet.oversea.entity.account.AppPushDeviceEntity;
import com.playlet.oversea.entity.template.EmailTemplateEntity;
import com.playlet.oversea.enums.*;
import com.playlet.oversea.filter.JWTAuthenticationFilter;
import com.playlet.oversea.query.account.BindPushQuery;
import com.playlet.oversea.query.account.PushSwitchQuery;
import com.playlet.oversea.query.account.UpdatePwdEntity;
import com.playlet.oversea.service.AppUserService;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.utils.*;
import com.playlet.oversea.utils.oidc.OidcIdTokenPayload;
import com.playlet.oversea.utils.oidc.OidcTokenVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class AppUserServiceImpl extends BaseApiService implements AppUserService {

	@Autowired
	private RedisUtil redisUtil;

	@Autowired
	private AppAccountDao appAccountDao;

	@Autowired
	private AppPushDeviceDao appPushDeviceDao;

	@Autowired
	private EmailTemplateDao emailTemplateDao;

	@Autowired
	private OauthLoginProperties oauthLoginProperties;

	@Autowired
	private AppOauthAccountDao appOauthAccountDao;

	@Autowired
	private MediaUrlService mediaUrlService;

	@Autowired
	private UserFollowDao userFollowDao;

	@Autowired
	private UserDramaLikeDao UserDramaLikeDao;

	@SuppressWarnings("deprecation")
	@Override
	public ResponseBase signUp(@RequestBody AppAccountEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getUserEmail())
				|| StringUtils.isEmpty(entity.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 校验邮箱验证码
		if (!verifyCode(entity.getUserEmail(), entity.getEmailCode())) {
			return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
		}
		// 邮箱唯一性校验
		if (appAccountDao.findByEmail(entity.getUserEmail()) != null) {
			return setResultError(I18nUtil.getMessage("user.account_exist"));
		}
		AppAccountEntity account = new AppAccountEntity();
		account.setUserAccount(entity.getUserEmail());
		account.setUserEmail(entity.getUserEmail());
		account.setUserPassword(PasswordHashUtils.encode(entity.getUserPassword()));
		account.setMobileNumber(entity.getMobileNumber());
		account.setMobilePrefix(entity.getMobilePrefix());
		Long seed = Long.parseLong(
				System.currentTimeMillis() +
						String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 9999))
		);
		account.setInvitationCode(RandomSuffixInviteCodeUtil.generateUniqueCode(seed, 4, 6));
		account.setRegisterSource(2);
		account.setRegistrationId(resolveRegistrationId(entity.getRegistrationId(), entity.getCid()));
		if (!StringUtils.isEmpty(entity.getDeviceName())) {
			account.setDeviceName(entity.getDeviceName().trim());
		}
		account.setUserState(UserStateEnums.NORMAL.getIndex());
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
		int randomNum = ThreadLocalRandom.current().nextInt(100, 999);
		account.setNickname("user_" + timestamp + randomNum);
		account.setSetTime(new Date());
		account.setGmtModified(new Date());
		appAccountDao.insert(account);
		upsertPushBind(account.getId(), account.getRegistrationId(), account.getDeviceName());
		String token = Jwts.builder()
				// 设置主题
				.setSubject(entity.getUserAccount())
				// 设置到期时间
				.setExpiration(new Date(System.currentTimeMillis() + Constants.USER_JWT_EXPIRE_TIME))
				// 选择 加密算法和私钥
				.signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY).compact();
		redisUtil.set(Constants.APP_PACKAGE_NAME + entity.getUserAccount(),
				Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME / 1000);
		return setResultSuccess(Constants.AUTH_HEADER_START_WITH + token,
				I18nUtil.getMessage("base_success"));
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
					&& PasswordHashUtils.matches(entity.getUserPassword(), appUserEntity.getUserPassword())) {
				if (PasswordHashUtils.needsRehash(appUserEntity.getUserPassword())) {
					appUserEntity.setUserPassword(PasswordHashUtils.encode(entity.getUserPassword()));
					appAccountDao.updateById(appUserEntity);
				}
				String token = Jwts.builder()
						// 设置主题
						.setSubject(appUserEntity.getUserAccount())
						// 设置到期时间
						.setExpiration(new Date(System.currentTimeMillis() + Constants.USER_JWT_EXPIRE_TIME))
						// 选择 加密算法和私钥
						.signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY).compact();
				redisUtil.set(Constants.APP_PACKAGE_NAME + appUserEntity.getUserAccount(),
						Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME / 1000);
				//保存更换jpush appUserEntity
				String registrationId = resolveRegistrationId(entity.getRegistrationId(), entity.getCid());
				String deviceName = StringUtils.isEmpty(entity.getDeviceName()) ? null : entity.getDeviceName().trim();
				if (registrationId != null) {
					appUserEntity.setRegistrationId(registrationId);
					if (deviceName != null) {
						appUserEntity.setDeviceName(deviceName);
					}
					appAccountDao.updateById(appUserEntity);
					upsertPushBind(appUserEntity.getId(), registrationId, deviceName);
				}
				return setResultSuccess(Constants.AUTH_HEADER_START_WITH + token, I18nUtil.getMessage("base_success"));
			} else {
				return setResultError(I18nUtil.getMessage("user.password_error"));
			}
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
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
					return setResultError(I18nUtil.getMessage("oauth.apple_not_configured"));
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
				return setResultError(I18nUtil.getMessage("oauth.google_not_configured"));
			}
			OidcIdTokenPayload payload = OidcTokenVerifier.verifyGoogleIdToken(entity.getIdToken(), googleClients);
			return thirdPartyLogin("google", payload, entity, request);
		} catch (Exception e) {
			log.warn("oneClickLogin 失败 type={}", type, e);
			TransactionUtils.markRollbackOnly();
			return setResultError(I18nUtil.getMessage(isApple ? "oauth.apple_login_failed" : "oauth.google_login_failed"));
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
			account = appAccountDao.selectById(binding.getId());
		}

		if (account == null) {
			String email = payload.getEmail();
			if (email != null && !email.trim().isEmpty()) {
				if ("google".equals(provider) && Boolean.FALSE.equals(payload.getEmailVerified())) {
					return setResultError(I18nUtil.getMessage("oauth.google_email_unverified"));
				}
				account = appAccountDao.findByEmail(email.trim());
			}
		}

		if (account == null) {
			String email = payload.getEmail() == null ? null : payload.getEmail().trim();
			if (email == null || email.isEmpty()) {
				return setResultError(I18nUtil.getMessage("oauth.email_required"));
			}
			if ("google".equals(provider) && Boolean.FALSE.equals(payload.getEmailVerified())) {
				return setResultError(I18nUtil.getMessage("oauth.google_email_unverified"));
			}
			UserRegisterEntity apiEntity = new UserRegisterEntity();
			apiEntity.setEmail(email);
			AppAccountEntity newEntity = new AppAccountEntity();
			newEntity.setUserEmail(email);
			newEntity.setUserAccount(email);
			newEntity.setUserPassword("");
			newEntity.setUserState(UserStateEnums.NORMAL.getIndex());
			this.addAccount(newEntity, RegisterSourceEnums.ONE_CLICK_LOGIN.getIndex());
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
				Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME / 1000);

		String registrationId = resolveRegistrationId(entity.getRegistrationId(), entity.getCid());
		String deviceName = StringUtils.isEmpty(entity.getDeviceName()) ? null : entity.getDeviceName().trim();
		if (registrationId != null) {
			account.setRegistrationId(registrationId);
			if (deviceName != null) {
				account.setDeviceName(deviceName);
			}
			appAccountDao.updateById(account);
			upsertPushBind(account.getId(), registrationId, deviceName);
		}

		return setResultSuccess(Constants.AUTH_HEADER_START_WITH + token, I18nUtil.getMessage("base_success"));
	}

	
	//创建各账户
	public void addAccount(AppAccountEntity entity,Integer source) {
		try {
			entity.setUserAccount(entity.getUserEmail());
			entity.setUserPassword(PasswordHashUtils.encode(entity.getUserPassword()));
			entity.setUserState(UserStateEnums.NORMAL.getIndex());
			//添加注册来源 1：一键注册用户 2:正常注册用户
			entity.setRegisterSource(source);
			entity.setInvitationCode(RandomSuffixInviteCodeUtil.generateUniqueCode(Long.parseLong(entity.getId().toString()), 4, 6));
			GenericityUtil.setDate(entity);
			appAccountDao.insert(entity);
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
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
			}
			if (!AppTokenUtil.isActiveSession(userData.getName(), header)) {
				return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
			}
			String username = userData.getName();
			AppAccountEntity entity = appAccountDao.findByAccount(username);
			entity.setUserPassword(null);
			entity.setPayPassword(null);
			entity.setGoogleSecretkey(null);
			entity.setFollowCount(userFollowDao.countFollowing(entity.getId()));
			entity.setFansCount(userFollowDao.countFans(entity.getId()));
			entity.setLikeCount(UserDramaLikeDao.countLike(entity.getId()));
			if (StringUtils.isEmpty(entity.getPushLangue())) {
				entity.setPushLangue(LanguageEnums.DEFAULT_LANGUE);
			} else {
				entity.setPushLangue(LanguageEnums.of(entity.getPushLangue()).getName());
			}
			String avatar = entity.getAvatar();
			entity.setAvatar(mediaUrlService.sign(avatar));
			return setResultSuccess(entity);
		} catch (Exception e) {
			log.error("service error", e);
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
				//添加动态数据（邮件/验证码做 HTML 转义，防 XSS）
				String htmlContent = MessageFormatUtils.format(
						templateEntity.getTemplateContent(),
						HtmlSanitizeUtils.plain(userEmail),
						HtmlSanitizeUtils.plain(code));
				//组装html内容
				String html = MessageFormatUtils.saveHtml(htmlContent, language);
				EmailUtil.sendEmail(userEmail, templateEntity.getTemplateSubject(), html);
				redisUtil.set(RedisKeyConstants.EMAIL_CODE_KEY + userEmail, code, Constants.CODE_EXPIRE_TIME);
				return setResultSuccess(I18nUtil.getMessage("send_success"));
			} else {
				return setResultError(I18nUtil.getMessage("Template_null"));
			}
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase checkEmailCode(String userEmail, String emailCode) {
		if (verifyCode(userEmail, emailCode)) {
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
	}

	@Override
	public ResponseBase updatePwd(@RequestBody UpdatePwdEntity entity, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
		}
		if (entity == null || StringUtils.isEmpty(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppAccountEntity account = appAccountDao.selectById(uid);
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		// 校验原密码
		if (StringUtils.isNotEmpty(account.getUserPassword())
				&& !PasswordHashUtils.matches(entity.getFormerPassword(), account.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("old_password_error"));
		}
		account.setUserPassword(PasswordHashUtils.encode(entity.getNewPassword()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		// 改密后踢全端，旧 token 立即失效（含当前设备，需重新登录）
		AppTokenUtil.invalidateAccountSessions(account);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase update(@RequestBody AppAccountEntity entity, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (entity == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 忽略 body 中的 id，强制使用 token 归属，防止 IDOR
		entity.setId(uid);
		entity.setNickname(HtmlSanitizeUtils.plain(entity.getNickname()));
		appAccountDao.updateNameById(entity);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}


	@Override
	public ResponseBase bindPush(@RequestBody BindPushQuery entity, HttpServletRequest request) {
		String registrationId = resolveRegistrationId(entity == null ? null : entity.getRegistrationId(),
				entity == null ? null : entity.getCid());
		if (StringUtils.isEmpty(registrationId)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String deviceName = entity == null || StringUtils.isEmpty(entity.getDeviceName())
				? null : entity.getDeviceName().trim();
		// 可选登录：有 token 则关联 uid 并写账号表
		Integer uid = AppTokenUtil.resolveUid(request);
		upsertPushBind(uid, registrationId, deviceName);
		if (uid != null) {
			appAccountDao.updatePushBind(uid, registrationId, deviceName);
			// 推送语言从请求头 language 读取（JWT 过滤器写入 LanguageContext）
			String langue = LanguageEnums.of(LanguageContext.getLanguage()).getName();
			appAccountDao.updatePushLangue(uid, langue);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase getPushSwitch(
			@org.springframework.web.bind.annotation.RequestParam(value = "registrationId", required = false) String registrationId,
			@org.springframework.web.bind.annotation.RequestParam(value = "cid", required = false) String cid) {
		String regId = resolveRegistrationId(registrationId, cid);
		if (StringUtils.isEmpty(regId)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppPushDeviceEntity device = appPushDeviceDao.findByRegistrationId(regId);
		int enabled = 1;
		if (device != null && device.getPushEnabled() != null) {
			enabled = Integer.valueOf(0).equals(device.getPushEnabled()) ? 0 : 1;
		}
		java.util.Map<String, Object> data = new java.util.HashMap<>();
		data.put("enabled", enabled);
		data.put("registrationId", regId);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase setPushSwitch(@RequestBody PushSwitchQuery entity) throws InvocationTargetException, IllegalAccessException {
		if (entity == null || entity.getEnabled() == null
				|| (!Integer.valueOf(0).equals(entity.getEnabled())
				&& !Integer.valueOf(1).equals(entity.getEnabled()))) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String regId = resolveRegistrationId(entity.getRegistrationId(), entity.getCid());
		if (StringUtils.isEmpty(regId)) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppPushDeviceEntity exist = appPushDeviceDao.findByRegistrationId(regId);
		if (exist == null) {
			AppPushDeviceEntity row = new AppPushDeviceEntity();
			row.setRegistrationId(regId);
			row.setPushEnabled(entity.getEnabled());
			GenericityUtil.setDate(row);
			appPushDeviceDao.insert(row);
		} else if (!entity.getEnabled().equals(exist.getPushEnabled())) {
			appPushDeviceDao.updatePushEnabled(regId, entity.getEnabled());
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** registrationId 优先，否则用 cid */
	private String resolveRegistrationId(String registrationId, String cid) {
		if (!StringUtils.isEmpty(registrationId)) {
			return registrationId.trim();
		}
		if (!StringUtils.isEmpty(cid)) {
			return cid.trim();
		}
		return null;
	}

	/**
	 * 写入/更新设备表。uid 可为空（游客）。
	 */
	private void upsertPushBind(Integer uid, String registrationId, String deviceName) {
		if (StringUtils.isEmpty(registrationId)) {
			return;
		}
		try {
			AppPushDeviceEntity exist = appPushDeviceDao.findByRegistrationId(registrationId);
			if (exist == null) {
				AppPushDeviceEntity row = new AppPushDeviceEntity();
				row.setRegistrationId(registrationId);
				row.setDeviceName(deviceName);
				row.setUid(uid);
				row.setPushEnabled(1);
				GenericityUtil.setDate(row);
				appPushDeviceDao.insert(row);
				return;
			}
			boolean changed = false;
			if (deviceName != null && !deviceName.equals(exist.getDeviceName())) {
				exist.setDeviceName(deviceName);
				changed = true;
			}
			if (uid != null && !uid.equals(exist.getUid())) {
				exist.setUid(uid);
				changed = true;
			}
			if (!changed) {
				return;
			}
			GenericityUtil.updateDate(exist);
			appPushDeviceDao.updateById(exist);
		} catch (Exception e) {
			log.warn("upsertPushBind failed registrationId={}: {}", registrationId, e.getMessage());
		}
	}

	@Override
	public ResponseBase signOut(HttpServletRequest request) {
		try {
			UsernamePasswordAuthenticationToken token = JWTAuthenticationFilter.getAuthentication(request);
			if (token == null) {
				return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
			}
			String username = token.getName();
			AppAccountEntity userEntity = appAccountDao.findByAccount(username);
			if (userEntity != null) {
				AppTokenUtil.invalidateAccountSessions(userEntity);
				// 退出清空账号推送绑定，设备表解绑 uid，避免串号
				appAccountDao.updatePushBind(userEntity.getId(), null, null);
				appPushDeviceDao.clearUid(userEntity.getId());
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			// 账号查不到仍按 JWT subject 删会话，避免残留
			AppTokenUtil.invalidateSessionByAccount(username);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase forgetPasswrod(@RequestBody UpdatePwdEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getEmail())
				|| StringUtils.isEmpty(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!verifyCode(entity.getEmail(), entity.getEmailCode())) {
			return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
		}
		AppAccountEntity account = appAccountDao.findByEmail(entity.getEmail());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		account.setUserPassword(PasswordHashUtils.encode(entity.getNewPassword()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		// 清理邮箱验证码
		redisUtil.del(RedisKeyConstants.EMAIL_CODE_KEY + entity.getEmail());
		// 忘记密码后踢全端
		AppTokenUtil.invalidateAccountSessions(account);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "app用户管理", type = "get", remark = "注销账户")
	public ResponseBase logout(Integer uid, HttpServletRequest request) {
		try {
			Integer tokenUid = AppTokenUtil.resolveUid(request);
			if (tokenUid == null) {
				return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"));
			}
			if (uid == null || !uid.equals(tokenUid)) {
				return setResultError(I18nUtil.getMessage("purview_error_null"));
			}
			AppAccountEntity userEntity = appAccountDao.selectById(tokenUid);
			if (userEntity != null) {
				userEntity.setUserState(UserStateEnums.LOGOUT.getIndex());
				appAccountDao.updateById(userEntity);
				AppTokenUtil.invalidateAccountSessions(userEntity);
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}
			return setResultError(I18nUtil.getMessage("base_error"));
		} catch (Exception e) {
			log.error("service error", e);
			throw new RuntimeException(e);
		}
	}

	// ==================== 私有工具方法 ====================

	/**
	 * 校验验证码
	 */
	private boolean verifyCode(String email, String code) {
		if (StringUtils.isEmpty(code)) {
			return false;
		}
		Object cache = redisUtil.get(RedisKeyConstants.EMAIL_CODE_KEY + email);
		return cache != null && cache.toString().equals(code);
	}

}
