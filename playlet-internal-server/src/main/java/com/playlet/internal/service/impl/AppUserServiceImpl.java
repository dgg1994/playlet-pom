package com.playlet.internal.service.impl;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.UserRegisterEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.OauthLoginProperties;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppOauthAccountDao;
import com.playlet.internal.dao.account.AppPushDeviceDao;
import com.playlet.internal.dao.account.UserFollowDao;
import com.playlet.internal.dao.drama.UserDramaLikeDao;
import com.playlet.internal.dao.template.EmailTemplateDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.AppOauthAccountEntity;
import com.playlet.internal.entity.account.AppPushDeviceEntity;
import com.playlet.internal.entity.template.EmailTemplateEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.filter.JWTAuthenticationFilter;
import com.playlet.internal.query.account.BindPushQuery;
import com.playlet.internal.query.account.PushSwitchQuery;
import com.playlet.internal.query.account.UpdatePwdEntity;
import com.playlet.internal.service.AppUserService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.*;
import com.playlet.internal.utils.oidc.OidcIdTokenPayload;
import com.playlet.internal.utils.oidc.OidcTokenVerifier;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@Transactional
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
		account.setUserPassword(DigestUtils.md5DigestAsHex((entity.getUserPassword()).getBytes()));
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
				Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME);
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
			account = appAccountDao.selectById(binding.getId());
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
				Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME);

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
			entity.setFollowCount(userFollowDao.countFollowing(entity.getId()));
			entity.setFansCount(userFollowDao.countFans(entity.getId()));
			entity.setLikeCount(UserDramaLikeDao.countLike(entity.getId()));
			if (entity.getPushEnabled() == null) {
				entity.setPushEnabled(1);
			}
			if (StringUtils.isEmpty(entity.getPushLangue())) {
				entity.setPushLangue(LanguageEnums.DEFAULT_LANGUE);
			} else {
				entity.setPushLangue(LanguageEnums.of(entity.getPushLangue()).getName());
			}
			String avatar = entity.getAvatar();
			entity.setAvatar(mediaUrlService.sign(avatar));
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
				redisUtil.set(userEmail, code, Constants.CODE_EXPIRE_TIME);
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
		if (verifyCode(userEmail, emailCode)) {
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		return setResultError("验证码错误或已过期");
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
				&& !DigestUtils.md5DigestAsHex((entity.getFormerPassword()).getBytes()).equals(account.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("old_password_error"));
		}
		account.setUserPassword(DigestUtils.md5DigestAsHex((entity.getNewPassword()).getBytes()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase update(@RequestBody AppAccountEntity entity, HttpServletRequest request) {
		Integer id = entity.getId();
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}

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
	public ResponseBase getPushSwitch(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		int enabled = 1;
		if (account != null && account.getPushEnabled() != null) {
			enabled = Integer.valueOf(0).equals(account.getPushEnabled()) ? 0 : 1;
		}
		java.util.Map<String, Object> data = new java.util.HashMap<>();
		data.put("enabled", enabled);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase setPushSwitch(@RequestBody PushSwitchQuery entity, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (entity == null || entity.getEnabled() == null
				|| (!Integer.valueOf(0).equals(entity.getEnabled())
				&& !Integer.valueOf(1).equals(entity.getEnabled()))) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		appAccountDao.updatePushEnabled(uid, entity.getEnabled());
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
				GenericityUtil.setDate(row);
				appPushDeviceDao.insert(row);
				return;
			}
			exist.setDeviceName(deviceName != null ? deviceName : exist.getDeviceName());
			if (uid != null) {
				exist.setUid(uid);
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
			if(token == null) {
				return setResult(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("token_error"), null);
			}
			String username = token.getName();
			AppAccountEntity userEntity = appAccountDao.findByEmail(username);
			if(userEntity != null) {
				redisUtil.del(Constants.APP_PACKAGE_NAME + userEntity.getUserEmail());
				// 退出清空账号推送绑定，设备表解绑 uid，避免串号
				appAccountDao.updatePushBind(userEntity.getId(), null, null);
				appPushDeviceDao.clearUid(userEntity.getId());
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}else {
				return setResultError(I18nUtil.getMessage("base_success"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public ResponseBase forgetPasswrod(@RequestBody UpdatePwdEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getEmail())
				|| StringUtils.isEmpty(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!verifyCode(entity.getEmail(), entity.getEmailCode())) {
			return setResultError("验证码错误或已过期");
		}
		AppAccountEntity account = appAccountDao.findByEmail(entity.getEmail());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		account.setUserPassword(DigestUtils.md5DigestAsHex((entity.getNewPassword()).getBytes()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		redisUtil.del(entity.getEmail());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "app用户管理", type = "get", remark = "注销账户")
	public ResponseBase logout(Integer uid,HttpServletRequest request) {
		try {
			UsernamePasswordAuthenticationToken token = JWTAuthenticationFilter.getAuthentication(request);
			String username = token.getName();
			AppAccountEntity userEntity = appAccountDao.findByEmail(username);
			if(userEntity != null ) {
				if(!uid.equals(userEntity.getId())) {
					return setResultError(I18nUtil.getMessage("purview_error_null"));
				}
				userEntity.setUserState(UserStateEnums.LOGOUT.getIndex());
				appAccountDao.updateById(userEntity);
				redisUtil.del(Constants.APP_PACKAGE_NAME + userEntity.getUserEmail());
				return setResultSuccess(I18nUtil.getMessage("base_success"));
			}else {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	// ==================== 私有工具方法 ====================

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

}
