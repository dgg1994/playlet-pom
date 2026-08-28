package com.playlet.internal.service.impl;

import com.playlet.internal.api.response.CreatorInfoRespEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorProfileDao;
import com.playlet.internal.dao.template.EmailTemplateDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import com.playlet.internal.entity.template.EmailTemplateEntity;
import com.playlet.internal.enums.*;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.creator.*;
import com.playlet.internal.service.CreatorAuthService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.service.third.WalletUserService;
import com.playlet.internal.utils.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 作家端注册登录。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorAuthServiceImpl extends BaseApiService implements CreatorAuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(CreatorConstants.EMAIL_REGEX);

    @Autowired
    private CreatorAccountDao creatorAccountDao;
    @Autowired
    private CreatorProfileDao creatorProfileDao;
    @Autowired
    private EmailTemplateDao emailTemplateDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MediaUrlService mediaUrlService;
    @Autowired
    private WalletUserService walletUserService;

    @Override
    public ResponseBase sendEmailCode(@RequestParam("userAccount") String userAccount,
                                      @RequestParam(value = "scene", required = false) Integer scene) {
        String email = normalizeEmail(userAccount);
        if (!isEmail(email)) {
            return setResultError(I18nUtil.getMessage("creator.email_invalid"));
        }
        CreatorEmailCodeSceneEnums sceneEnum = CreatorEmailCodeSceneEnums.fromCode(scene);
        if (sceneEnum == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        CreatorAccountEntity exist = creatorAccountDao.findByAccount(email);
        if (sceneEnum == CreatorEmailCodeSceneEnums.REGISTER && exist != null) {
            return setResultError(I18nUtil.getMessage("user.account_exist"));
        }
        if (sceneEnum == CreatorEmailCodeSceneEnums.RESET_PWD && exist == null) {
            return setResultError(I18nUtil.getMessage("user.account_error"));
        }
        try {
            String language = LanguageContext.getLanguage();
            EmailTemplateEntity templateEntity = emailTemplateDao.findByNum(
                    MessageEnums.SEND_CODE_ZH.getIndex(), language);
            if (templateEntity == null || StringUtils.isEmpty(templateEntity.getTemplateContent())) {
                return setResultError(I18nUtil.getMessage("Template_null"));
            }
            String code = OrderCodeFactory.getRandomStr(6);
            String htmlContent = MessageFormatUtils.format(
                    templateEntity.getTemplateContent(),
                    HtmlSanitizeUtils.plain(email),
                    HtmlSanitizeUtils.plain(code));
            String html = MessageFormatUtils.saveHtml(htmlContent, language);
            EmailUtil.sendEmail(email, templateEntity.getTemplateSubject(), html);
            redisUtil.set(RedisKeyConstants.CREATOR_EMAIL_CODE_KEY + email, code, Constants.CODE_EXPIRE_TIME);
            log.info("creator email code sent scene={} email={}", sceneEnum.getCode(), maskEmail(email));
            return setResultSuccess(I18nUtil.getMessage("send_success"));
        } catch (Exception e) {
            log.error("creator send email code failed email={}", maskEmail(email), e);
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResponseBase signUp(@Valid @RequestBody CreatorSignUpQuery query) {
        String email = normalizeEmail(query.getUserAccount());
        if (!isEmail(email) || StringUtils.isEmpty(query.getUserPassword())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        if (StringUtils.isNotEmpty(query.getConfirmPassword())
                && !query.getUserPassword().equals(query.getConfirmPassword())) {
            return setResultError(I18nUtil.getMessage("creator.password_not_match"));
        }
        if (!verifyCode(email, query.getEmailCode())) {
            return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
        }
        if (query.getIdentityType() != null && !CreatorIdentityTypeEnums.isValid(query.getIdentityType())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        if (creatorAccountDao.findByAccount(email) != null) {
            return setResultError(I18nUtil.getMessage("user.account_exist"));
        }
        Date now = new Date();
        CreatorAccountEntity account = new CreatorAccountEntity();
        account.setUserAccount(email);
        account.setUserPassword(PasswordHashUtils.encode(query.getUserPassword()));
        account.setMobilePrefix(trimToNull(query.getMobilePrefix()));
        account.setMobileNumber(trimToNull(query.getMobileNumber()));
        // 注册昵称与 C 端一致：user_ + yyMMddHHmmss + 3 位随机数
        account.setNickname(query.getNickname());
        account.setUserState(UserStateEnums.NORMAL.getIndex());
        account.setCoinBalance(0L);
        account.setFrozenCoinBalance(0L);
        account.setTotalIncomeCoin(0L);
        account.setLastLoginTime(now);
        account.setSetTime(now);
        account.setGmtModified(now);
        try {
            creatorAccountDao.insert(account);
            creatorProfileDao.insert(buildProfile(account.getId(), query, now));
            // 作家注册后开通钱包三方用户（P0）
            walletUserService.registerOnSignUp(WithdrawUserTypeEnums.CREATOR.getCode(), account.getId(),
                    email, account.getMobilePrefix(), account.getMobileNumber());
        } catch (BaseException e) {
            log.error("creator wallet register failed email={}", maskEmail(email), e);
            throw e;
        } catch (Exception e) {
            log.error("creator signUp failed email={}", maskEmail(email), e);
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
        consumeCode(email);
        String token = issueToken(email);
        log.info("creator signUp creatorId={} email={}", account.getId(), maskEmail(email));
        return setResultSuccess(token, I18nUtil.getMessage("base_success"));
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResponseBase login(@Valid @RequestBody CreatorLoginQuery query) {
        String email = normalizeEmail(query.getUserAccount());
        if (!isEmail(email) || StringUtils.isEmpty(query.getUserPassword())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        CreatorAccountEntity account = creatorAccountDao.findByAccount(email);
        if (account == null) {
            return setResultError(I18nUtil.getMessage("user.account_error"));
        }
        if (UserStateEnums.LOGOUT.getIndex().equals(account.getUserState())) {
            return setResultError(I18nUtil.getMessage("user.account_null"));
        }
        if (UserStateEnums.DISABLE.getIndex().equals(account.getUserState())) {
            return setResultError(I18nUtil.getMessage("creator.account_frozen"));
        }
        if (!PasswordHashUtils.matches(query.getUserPassword(), account.getUserPassword())) {
            return setResultError(I18nUtil.getMessage("user.password_error"));
        }
        if (PasswordHashUtils.needsRehash(account.getUserPassword())) {
            account.setUserPassword(PasswordHashUtils.encode(query.getUserPassword()));
            account.setGmtModified(new Date());
            try {
                creatorAccountDao.updateById(account);
            } catch (Exception e) {
                log.error("creator rehash password failed creatorId={}", account.getId(), e);
                throw new BaseException(I18nUtil.getMessage("base_error"), e);
            }
        }
        try {
            creatorAccountDao.updateLastLoginTime(account.getId());
        } catch (Exception e) {
            log.error("creator update last login failed creatorId={}", account.getId(), e);
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
        String token = issueToken(email);
        log.info("creator login creatorId={}", account.getId());
        return setResultSuccess(token, I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase forgetPassword(@Valid @RequestBody CreatorForgetPwdQuery query) {
        String email = normalizeEmail(query.getUserAccount());
        if (!isEmail(email) || StringUtils.isEmpty(query.getNewPassword())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        if (!verifyCode(email, query.getEmailCode())) {
            return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
        }
        CreatorAccountEntity account = creatorAccountDao.findByAccount(email);
        if (account == null) {
            return setResultError(I18nUtil.getMessage("user.account_error"));
        }
        account.setUserPassword(PasswordHashUtils.encode(query.getNewPassword()));
        account.setGmtModified(new Date());
        try {
            creatorAccountDao.updateById(account);
        } catch (Exception e) {
            log.error("creator forgetPassword failed creatorId={}", account.getId(), e);
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
        consumeCode(email);
        CreatorTokenUtil.invalidateSession(email);
        log.info("creator forgetPassword creatorId={}", account.getId());
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase updatePwd(@Valid @RequestBody CreatorUpdatePwdQuery query, HttpServletRequest request) {
        CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
        if (account == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        if (!PasswordHashUtils.matches(query.getFormerPassword(), account.getUserPassword())) {
            return setResultError(I18nUtil.getMessage("old_password_error"));
        }
        account.setUserPassword(PasswordHashUtils.encode(query.getNewPassword()));
        account.setGmtModified(new Date());
        try {
            creatorAccountDao.updateById(account);
        } catch (Exception e) {
            log.error("creator updatePwd failed creatorId={}", account.getId(), e);
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
        CreatorTokenUtil.invalidateSession(account.getUserAccount());
        log.info("creator updatePwd creatorId={}", account.getId());
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase update(@RequestBody CreatorUpdateInfoQuery query, HttpServletRequest request) {
        CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
        if (account == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        if (query == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        String nicknameErr = validateNickname(query.getNickname());
        if (nicknameErr != null) {
            return setResultError(nicknameErr);
        }
        if (query.getIdentityType() != null && !CreatorIdentityTypeEnums.isValid(query.getIdentityType())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        Date now = new Date();
        if (query.getNickname() != null) {
            account.setNickname(resolveNickname(query.getNickname(), account.getUserAccount()));
        }
        if (query.getAvatar() != null) {
            account.setAvatar(trimToNull(query.getAvatar()));
        }
        if (query.getMobilePrefix() != null) {
            account.setMobilePrefix(trimToNull(query.getMobilePrefix()));
        }
        if (query.getMobileNumber() != null) {
            account.setMobileNumber(trimToNull(query.getMobileNumber()));
        }
        account.setGmtModified(now);
        CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(account.getId());
        if (profile == null) {
            profile = emptyProfile(account.getId(), now);
            try {
                creatorProfileDao.insert(profile);
            } catch (Exception e) {
                log.error("creator insert profile failed creatorId={}", account.getId(), e);
                throw new BaseException(I18nUtil.getMessage("base_error"), e);
            }
        }
        applyProfileUpdate(profile, query, now);
        try {
            creatorAccountDao.updateById(account);
            creatorProfileDao.updateById(profile);
        } catch (DuplicateKeyException e) {
            log.error("creator update duplicate creatorId={}", account.getId(), e);
            return setResultError(I18nUtil.getMessage("base_info_exist"));
        } catch (Exception e) {
            log.error("creator update failed creatorId={}", account.getId(), e);
            throw new BaseException(I18nUtil.getMessage("base_error"), e);
        }
        log.info("creator update info creatorId={}", account.getId());
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase findInfo(HttpServletRequest request) {
        CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
        if (account == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        return setResultSuccess(toInfoResp(account, creatorProfileDao.findByCreatorId(account.getId())));
    }

    @Override
    public ResponseBase signOut(HttpServletRequest request) {
        CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
        if (account == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        CreatorTokenUtil.invalidateSession(account.getUserAccount());
        log.info("creator signOut creatorId={}", account.getId());
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @SuppressWarnings("deprecation")
    private String issueToken(String email) {
        String token = Jwts.builder()
                .setSubject(CreatorTokenUtil.jwtSubject(email))
                .setExpiration(new Date(System.currentTimeMillis() + Constants.USER_JWT_EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS512, Constants.SIGNING_KEY)
                .compact();
        String bearer = Constants.AUTH_HEADER_START_WITH + token;
        redisUtil.set(CreatorTokenUtil.sessionKey(email), bearer, Constants.USER_REDIS_EXPIRE_TIME / 1000);
        return bearer;
    }

    private boolean verifyCode(String email, String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        Object cache = redisUtil.get(RedisKeyConstants.CREATOR_EMAIL_CODE_KEY + email);
        return cache != null && cache.toString().equals(code);
    }

    private void consumeCode(String email) {
        redisUtil.del(RedisKeyConstants.CREATOR_EMAIL_CODE_KEY + email);
    }

    private CreatorProfileEntity buildProfile(Integer creatorId, CreatorSignUpQuery query, Date now) {
        CreatorProfileEntity profile = emptyProfile(creatorId, now);
        if (query.getIdentityType() != null) {
            profile.setIdentityType(query.getIdentityType());
        }
        profile.setIdCardFront(trimToNull(query.getIdCardFront()));
        profile.setIdCardBack(trimToNull(query.getIdCardBack()));
        profile.setBillAddress(trimToNull(query.getBillAddress()));
        profile.setOrgName(trimToNull(query.getOrgName()));
        profile.setOrgLicense(trimToNull(query.getOrgLicense()));
        return profile;
    }

    private CreatorProfileEntity emptyProfile(Integer creatorId, Date now) {
        CreatorProfileEntity profile = new CreatorProfileEntity();
        profile.setCreatorId(creatorId);
        profile.setIdentityType(CreatorIdentityTypeEnums.PERSONAL.getCode());
        profile.setAuditStatus(CreatorProfileAuditStatusEnums.PENDING.getCode());
        profile.setSetTime(now);
        profile.setGmtModified(now);
        return profile;
    }

    private void applyProfileUpdate(CreatorProfileEntity profile, CreatorUpdateInfoQuery query, Date now) {
        if (query.getIdentityType() != null) {
            profile.setIdentityType(query.getIdentityType());
        }
        if (query.getIdCardFront() != null) {
            profile.setIdCardFront(trimToNull(query.getIdCardFront()));
        }
        if (query.getIdCardBack() != null) {
            profile.setIdCardBack(trimToNull(query.getIdCardBack()));
        }
        if (query.getBillAddress() != null) {
            profile.setBillAddress(trimToNull(query.getBillAddress()));
        }
        if (query.getOrgName() != null) {
            profile.setOrgName(trimToNull(query.getOrgName()));
        }
        if (query.getOrgLicense() != null) {
            profile.setOrgLicense(trimToNull(query.getOrgLicense()));
        }
        profile.setGmtModified(now);
    }

    private CreatorInfoRespEntity toInfoResp(CreatorAccountEntity account, CreatorProfileEntity profile) {
        CreatorInfoRespEntity resp = new CreatorInfoRespEntity();
        resp.setId(account.getId());
        resp.setUserAccount(account.getUserAccount());
        resp.setNickname(account.getNickname());
        resp.setAvatar(mediaUrlService.sign(account.getAvatar()));
        resp.setMobilePrefix(account.getMobilePrefix());
        resp.setMobileNumber(account.getMobileNumber());
        resp.setUserState(account.getUserState());
        resp.setCoinBalance(nvl(account.getCoinBalance()));
        resp.setFrozenCoinBalance(nvl(account.getFrozenCoinBalance()));
        resp.setAvailableCoin(nvl(account.getCoinBalance()) - nvl(account.getFrozenCoinBalance()));
        resp.setTotalIncomeCoin(nvl(account.getTotalIncomeCoin()));
        resp.setLastLoginTime(account.getLastLoginTime());
        resp.setSetTime(account.getSetTime());
        if (profile != null) {
            resp.setIdentityType(profile.getIdentityType());
            resp.setBillAddress(profile.getBillAddress());
            resp.setAuditStatus(profile.getAuditStatus());
            resp.setAuditRejectReason(profile.getAuditRejectReason());
            resp.setOrgName(profile.getOrgName());
            resp.setIdCardFront(mediaUrlService.sign(profile.getIdCardFront()));
            resp.setIdCardBack(mediaUrlService.sign(profile.getIdCardBack()));
        }
        // 钱包概要并入 findInfo，未开通则为 null
        resp.setWalletInfo(walletUserService.getInfoOrNull(
                WithdrawUserTypeEnums.CREATOR.getCode(), account.getId()));
        return resp;
    }

    /** 与 C 端注册一致：user_ + yyMMddHHmmss + 100~998 随机数 */
    private String generateAutoNickname() {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(CreatorConstants.NICKNAME_AUTO_TIME_PATTERN));
        int randomNum = ThreadLocalRandom.current().nextInt(
                CreatorConstants.NICKNAME_AUTO_RANDOM_ORIGIN, CreatorConstants.NICKNAME_AUTO_RANDOM_BOUND);
        return CreatorConstants.NICKNAME_AUTO_PREFIX + timestamp + randomNum;
    }

    private String validateNickname(String nickname) {
        if (nickname == null) {
            return null;
        }
        String plain = HtmlSanitizeUtils.plain(nickname.trim());
        if (plain != null && plain.length() > CreatorConstants.NICKNAME_MAX_LEN) {
            return I18nUtil.getMessage("creator.nickname_too_long");
        }
        return null;
    }

    private String resolveNickname(String nickname, String email) {
        if (StringUtils.isNotEmpty(nickname)) {
            return HtmlSanitizeUtils.plain(nickname.trim());
        }
        int at = email.indexOf('@');
        String prefix = at > 0 ? email.substring(0, at) : email;
        if (prefix.length() > CreatorConstants.NICKNAME_MAX_LEN) {
            return prefix.substring(0, CreatorConstants.NICKNAME_MAX_LEN);
        }
        return prefix;
    }

    private static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private static boolean isEmail(String email) {
        return StringUtils.isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trim = value.trim();
        return trim.isEmpty() ? null : trim;
    }

    private static long nvl(Long value) {
        return value == null ? 0L : value;
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        String name = email.substring(0, at);
        String domain = email.substring(at);
        if (name.length() <= 1) {
            return "*" + domain;
        }
        return name.charAt(0) + "***" + domain;
    }
}
