package com.playlet.internal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.UserRegisterEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.OauthLoginProperties;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.AppOauthAccountDao;
import com.playlet.internal.dao.account.AppPushDeviceDao;
import com.playlet.internal.dao.account.UserFollowDao;
import com.playlet.internal.dao.coupon.UserCouponDao;
import com.playlet.internal.dao.drama.UserDramaLikeDao;
import com.playlet.internal.dao.member.UserMemberDao;
import com.playlet.internal.dao.template.EmailTemplateDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.AppOauthAccountEntity;
import com.playlet.internal.entity.account.AppPushDeviceEntity;
import com.playlet.internal.entity.coupon.UserCouponEntity;
import com.playlet.internal.entity.member.UserMemberEntity;
import com.playlet.internal.entity.template.EmailTemplateEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.filter.JWTAuthenticationFilter;
import com.playlet.internal.query.account.BindPushQuery;
import com.playlet.internal.query.account.PushSwitchQuery;
import com.playlet.internal.query.account.UpdatePwdEntity;
import com.playlet.internal.service.AppUserService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.support.SmsSendSupport;
import com.playlet.internal.service.support.UserActiveStatService;
import com.playlet.internal.service.support.UserOnlineHeartbeatService;
import com.playlet.internal.utils.*;
import com.playlet.internal.utils.oidc.OidcIdTokenPayload;
import com.playlet.internal.utils.oidc.OidcTokenVerifier;
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
import java.util.regex.Pattern;

@Slf4j
@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
public class AppUserServiceImpl extends BaseApiService implements AppUserService {

	private static final Pattern BIRTH_MONTH_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

	/** 手机号（不含区号） */
	private static final Pattern MOBILE_PATTERN = Pattern.compile("^\\d{5,20}$");
	/** 国家区号，允许 86 或 +86 */
	private static final Pattern MOBILE_PREFIX_PATTERN = Pattern.compile("^\\+?\\d{1,5}$");
	/** 登录密码：6~20 位字母、数字及常用符号 */
	private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[A-Za-z0-9@#!$%^&*()_+\\-=.]{6,20}$");

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

	@Autowired
	private UserOnlineHeartbeatService userOnlineHeartbeatService;

	@Autowired
	private UserActiveStatService userActiveStatService;

	@Autowired
	private UserMemberDao userMemberDao;

	@Autowired
	private UserCouponDao userCouponDao;

	@Autowired
	private SmsSendSupport smsSendSupport;


	@SuppressWarnings("deprecation")
	@Override
	public ResponseBase signUp(@RequestBody AppAccountEntity entity) {
        try {
            if (entity == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            String mobilePrefix = normalizeMobilePrefix(entity.getMobilePrefix());
            String mobileNumber = trimToNull(entity.getMobileNumber());
            // 优先手机号注册；未传手机号时兼容旧的邮箱注册
            boolean byPhone = isValidMobile(mobilePrefix, mobileNumber);
            if (!byPhone && StringUtils.isEmpty(entity.getUserEmail())) {
                return setResultError(I18nUtil.getMessage("phone_format_error"));
            }
            if (!isValidPassword(entity.getUserPassword())) {
                return setResultError(I18nUtil.getMessage("password_format_error"));
            }
            if (byPhone) {
                // 校验短信验证码
//                if (!verifySmsCode(mobilePrefix, mobileNumber, entity.getTelCode())) {
//                    return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
//                }
                // 手机号唯一性校验
                if (appAccountDao.findByTel(mobileNumber, mobilePrefix) != null
                        || appAccountDao.findByAccount(mobileNumber) != null) {
                    return setResultError(I18nUtil.getMessage("user.account_exist"));
                }
            } else {
                // 校验邮箱验证码
                if (!verifyCode(entity.getUserEmail(), entity.getEmailCode())) {
                    return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
                }
                // 邮箱唯一性校验
                if (appAccountDao.findByEmail(entity.getUserEmail()) != null) {
                    return setResultError(I18nUtil.getMessage("user.account_exist"));
                }
            }
            AppAccountEntity account = new AppAccountEntity();
            if (byPhone) {
                account.setUserAccount(mobileNumber);
                account.setMobileNumber(mobileNumber);
                account.setMobilePrefix(mobilePrefix);
            } else {
                account.setUserAccount(entity.getUserEmail());
                account.setUserEmail(entity.getUserEmail());
                account.setMobileNumber(entity.getMobileNumber());
                account.setMobilePrefix(mobilePrefix);
            }
            account.setUserPassword(PasswordHashUtils.encode(entity.getUserPassword()));
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
            // 验证码一次性：注册成功后立即失效
            if (byPhone) {
                redisUtil.del(smsCodeKey(mobilePrefix, mobileNumber));
            } else {
                redisUtil.del(RedisKeyConstants.EMAIL_CODE_KEY + entity.getUserEmail());
            }
            String token = Jwts.builder()
                    // 设置主题
                    .setSubject(account.getUserAccount())
                    // 设置到期时间
                    .setExpiration(new Date(System.currentTimeMillis() + Constants.USER_JWT_EXPIRE_TIME))
                    // 选择 加密算法和私钥
                    .signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY).compact();
            redisUtil.set(Constants.APP_PACKAGE_NAME + account.getUserAccount(),
                    Constants.AUTH_HEADER_START_WITH + token, Constants.USER_REDIS_EXPIRE_TIME / 1000);
			// 注册即登录：写入 last_login_time，避免久未登录清币误伤
			appAccountDao.touchLastLoginTime(account.getId());
            return setResultSuccess(Constants.AUTH_HEADER_START_WITH + token,
                    I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@SuppressWarnings("deprecation")
	@Override
	public ResponseBase login(@RequestBody AppAccountEntity entity, HttpServletRequest req) {
		try {
			// 不传 loginType 时默认手机号+密码登录
			Integer loginType = entity.getLoginType() == null
					? PublicEnums.TOW.getIndex() : entity.getLoginType();
			AppAccountEntity appUserEntity = null;
			if(PublicEnums.ONE.getIndex().equals(loginType)) {//邮箱登录
				appUserEntity = appAccountDao.findByEmail(entity.getUserAccount());
			}else if(PublicEnums.TOW.getIndex().equals(loginType)) {//手机号登录
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
				// 在线按请求头设备 ID；无则回退极光 ID
				String deviceId = UserOnlineHeartbeatService.resolveFromRequest(req);
				if (deviceId == null) {
					deviceId = registrationId;
				}
				if (deviceId != null) {
					userOnlineHeartbeatService.heartbeat(deviceId, appUserEntity.getId());
				} else {
					userActiveStatService.markActive(appUserEntity.getId());
				}
				// 刷新最近登录，供久未登录清币判定
				appAccountDao.touchLastLoginTime(appUserEntity.getId());
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

		String deviceId = UserOnlineHeartbeatService.resolveFromRequest(request);
		if (deviceId == null) {
			deviceId = registrationId;
		}
		if (deviceId != null) {
			userOnlineHeartbeatService.heartbeat(deviceId, account.getId());
		} else {
			userActiveStatService.markActive(account.getId());
		}
		// 刷新最近登录，供久未登录清币判定
		appAccountDao.touchLastLoginTime(account.getId());
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
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
			int randomNum = ThreadLocalRandom.current().nextInt(100, 999);
			entity.setNickname("user_" + timestamp + randomNum);
			//entity.setInvitationCode(RandomSuffixInviteCodeUtil.generateUniqueCode(Long.parseLong(entity.getId().toString()), 4, 6));
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
			// vip相关
			UserMemberEntity userMember = userMemberDao.selectOne(new QueryWrapper<UserMemberEntity>().eq("uid", entity.getId()));
			entity.setIsVip(userMember != null ? userMember.getStatus() : 0);
			// 优惠卷数量
			Integer count = userCouponDao.selectCount(new QueryWrapper<UserCouponEntity>().eq("uid", entity.getId()).eq("status",0));
			entity.setCouponCount(count);
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
	public ResponseBase sendSmsCode(String mobilePrefix, String mobileNumber) {
		try {
			String prefix = normalizeMobilePrefix(mobilePrefix);
			String mobile = trimToNull(mobileNumber);
			if (!isValidMobile(prefix, mobile)) {
				return setResultError(I18nUtil.getMessage("phone_format_error"));
			}
			String fullMobile = toE164(prefix, mobile);
			// 60 秒冷却，防止刷短信
			if (redisUtil.hasKey(RedisKeyConstants.SMS_COOLDOWN_KEY + fullMobile)) {
				return setResultError(I18nUtil.getMessage("sms_send_too_frequent"));
			}
			String code = OrderCodeFactory.getRandomStr(6);
			String provider = smsSendSupport.resolveProvider(prefix);
			if (!smsSendSupport.sendVerificationCode(prefix, mobile, code)) {
				log.warn("短信验证码发送失败 provider={} mobile={}", provider, fullMobile);
				return setResultError(I18nUtil.getMessage("sms_send_failed"));
			}
			redisUtil.set(smsCodeKey(prefix, mobile), code, Constants.CODE_EXPIRE_TIME);
			redisUtil.set(RedisKeyConstants.SMS_COOLDOWN_KEY + fullMobile, "1",
					RedisKeyConstants.SMS_COOLDOWN_SEC);
			return setResultSuccess(I18nUtil.getMessage("send_success"));
		} catch (Exception e) {
			log.error("sendSmsCode error mobile={}", mobileNumber, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase checkSmsCode(String mobilePrefix, String mobileNumber, String smsCode) {
		String prefix = normalizeMobilePrefix(mobilePrefix);
		String mobile = trimToNull(mobileNumber);
		if (!isValidMobile(prefix, mobile)) {
			return setResultError(I18nUtil.getMessage("phone_format_error"));
		}
		if (verifySmsCode(prefix, mobile, smsCode)) {
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
		if (entity == null || !isValidPassword(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("password_format_error"));
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
		if (!UserGenderEnums.isValid(entity.getGender())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		AppAccountEntity existing = appAccountDao.selectById(uid);
		if (existing == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		AppAccountEntity update = new AppAccountEntity();
		update.setId(uid);
		if (entity.getNickname() != null) {
			update.setNickname(HtmlSanitizeUtils.plain(entity.getNickname()));
		} else {
			update.setNickname(existing.getNickname());
		}
		if (entity.getAvatar() != null) {
			update.setAvatar(entity.getAvatar());
		} else {
			update.setAvatar(existing.getAvatar());
		}
		if (entity.getGender() != null) {
			update.setGender(entity.getGender());
		} else {
			update.setGender(existing.getGender());
		}
		if (entity.getBirthMonth() != null) {
			String birthMonth = entity.getBirthMonth().trim();
			if (birthMonth.isEmpty()) {
				update.setBirthMonth(null);
			} else {
				if (!BIRTH_MONTH_PATTERN.matcher(birthMonth).matches()) {
					return setResultError(I18nUtil.getMessage("base_error"));
				}
				update.setBirthMonth(birthMonth);
			}
		} else {
			update.setBirthMonth(existing.getBirthMonth());
		}
		appAccountDao.updateProfileById(update);
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
	public ResponseBase heartbeat(HttpServletRequest request) {
		String deviceId = UserOnlineHeartbeatService.resolveFromRequest(request);
		if (deviceId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Integer uid = AppTokenUtil.resolveUid(request);
		userOnlineHeartbeatService.heartbeat(deviceId, uid);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase forgetPasswrod(@RequestBody UpdatePwdEntity entity) {
		if (entity == null || StringUtils.isEmpty(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (!isValidPassword(entity.getNewPassword())) {
			return setResultError(I18nUtil.getMessage("password_format_error"));
		}
		String mobilePrefix = normalizeMobilePrefix(entity.getMobilePrefix());
		String mobileNumber = trimToNull(entity.getMobileNumber());
		// 优先手机号+短信验证码；未传手机号时兼容邮箱验证码找回
		boolean byPhone = isValidMobile(mobilePrefix, mobileNumber);
		if (!byPhone && StringUtils.isEmpty(entity.getEmail())) {
			return setResultError(I18nUtil.getMessage("phone_format_error"));
		}
		AppAccountEntity account;
		if (byPhone) {
			if (!verifySmsCode(mobilePrefix, mobileNumber, entity.getTelCode())) {
				return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
			}
			account = appAccountDao.findByTel(mobileNumber, mobilePrefix);
		} else {
			if (!verifyCode(entity.getEmail(), entity.getEmailCode())) {
				return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
			}
			account = appAccountDao.findByEmail(entity.getEmail());
		}
		if (account == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		account.setUserPassword(PasswordHashUtils.encode(entity.getNewPassword()));
		account.setGmtModified(new Date());
		appAccountDao.updateById(account);
		// 清理验证码，保证一次性
		if (byPhone) {
			redisUtil.del(smsCodeKey(mobilePrefix, mobileNumber));
		} else {
			redisUtil.del(RedisKeyConstants.EMAIL_CODE_KEY + entity.getEmail());
		}
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
	 * 校验邮箱验证码
	 */
	private boolean verifyCode(String email, String code) {
		if (StringUtils.isEmpty(code)) {
			return false;
		}
		Object cache = redisUtil.get(RedisKeyConstants.EMAIL_CODE_KEY + email);
		return cache != null && cache.toString().equals(code);
	}

	/**
	 * 校验短信验证码
	 */
	private boolean verifySmsCode(String mobilePrefix, String mobileNumber, String code) {
		if (StringUtils.isEmpty(code)) {
			return false;
		}
		Object cache = redisUtil.get(smsCodeKey(mobilePrefix, mobileNumber));
		return cache != null && cache.toString().equals(code.trim());
	}

	/** 短信验证码 Redis key：区号+手机号，保证跨国同号不串 */
	private String smsCodeKey(String mobilePrefix, String mobileNumber) {
		return RedisKeyConstants.SMS_CODE_KEY + mobilePrefix + mobileNumber;
	}

	/** 区号：仅去空格，保持与库里 mobile_prefix 一致的写法 */
	private String normalizeMobilePrefix(String mobilePrefix) {
		return mobilePrefix == null ? "" : mobilePrefix.trim();
	}

	private String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		return v.isEmpty() ? null : v;
	}

	private boolean isValidMobile(String mobilePrefix, String mobileNumber) {
		return MOBILE_PREFIX_PATTERN.matcher(mobilePrefix).matches()
				&& mobileNumber != null && MOBILE_PATTERN.matcher(mobileNumber).matches();
	}

	/** 短信通道要求：区号+号码，不带 + */
	private String toE164(String mobilePrefix, String mobileNumber) {
		String prefix = mobilePrefix.startsWith("+") ? mobilePrefix.substring(1) : mobilePrefix;
		return prefix + mobileNumber;
	}

	private boolean isValidPassword(String password) {
		return password != null && PASSWORD_PATTERN.matcher(password).matches();
	}

	/** 登录邮箱：优先 user_email */
	private static String resolveLoginEmail(AppAccountEntity account) {
		if (account == null) {
			return "";
		}
		if (!StringUtils.isEmpty(account.getUserEmail())) {
			return account.getUserEmail();
		}
		return account.getUserAccount() == null ? "" : account.getUserAccount();
	}

}
