package com.playlet.internal.service.creator.impl;

import com.playlet.internal.api.response.CreatorAccountDetailResp;
import com.playlet.internal.api.response.CreatorIdentityDetailResp;
import com.playlet.internal.api.response.CreatorSettingResp;
import com.playlet.internal.api.response.CreatorWechatBindResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.CreatorConstants;
import com.playlet.internal.constants.RedisKeyConstants;
import com.playlet.internal.dao.creator.*;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.creator.*;
import com.playlet.internal.enums.CreatorCancelStatusEnums;
import com.playlet.internal.enums.CreatorIdentityAuditStatusEnums;
import com.playlet.internal.enums.CreatorIdentityTypeEnums;
import com.playlet.internal.enums.CreatorProfileAuditStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.creator.*;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.creator.CreatorAccountService;
import com.playlet.internal.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 账号信息（作家端）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorAccountServiceImpl extends BaseApiService implements CreatorAccountService {

	private static final int COOL_DOWN_DAYS = 7;
	private static final DateTimeFormatter FMT_DAYS = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter FMT_FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final List<Integer> IDENTITY_TYPES = Arrays.asList(1, 2, 3, 4);
	private static final String[] IDENTITY_NAMES = {"", "编剧身份", "演员身份", "导演/制作身份", "编剧机构"};
	private static Pattern EMAIL_PATTERN = Pattern.compile(CreatorConstants.EMAIL_REGEX);


	@Autowired
	private CreatorAccountDao creatorAccountDao;
	@Autowired
	private CreatorProfileDao creatorProfileDao;
	@Autowired
	private CreatorStatDao creatorStatDao;
	@Autowired
	private CreatorSocialBindDao creatorSocialBindDao;
	@Autowired
	private CreatorIdentityDao creatorIdentityDao;
	@Autowired
	private CreatorSettingDao creatorSettingDao;
	@Autowired
	private CreatorCancelApplyDao creatorCancelApplyDao;
	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private MediaUrlService mediaUrlService;
	@Autowired
	private RedisUtil redisUtil;

	@Value("${creator.identity.aes-key:${script.contact.aes-key:}}")
	private String identityAesKey;

	@Override
	public ResponseBase detail(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorAccountDetailResp resp = new CreatorAccountDetailResp();
		CreatorProfileEntity profile = creatorProfileDao.findByCreatorId(account.getId());
		CreatorStatEntity stat = creatorStatDao.findByCreatorId(account.getId());
		CreatorSettingEntity setting = creatorSettingDao.findByCreatorId(account.getId());
		CreatorCancelApplyEntity apply = creatorCancelApplyDao.findLatestByCreator(account.getId());

		// overview
		CreatorAccountDetailResp.Overview overview = new CreatorAccountDetailResp.Overview();
		overview.setCreatorId(account.getId());
		overview.setNickname(account.getNickname());
		overview.setAvatarUrl(mediaUrlService.sign(account.getAvatar()));
		overview.setScore(stat != null && stat.getScore() != null ? stat.getScore() : BigDecimal.ZERO);
		overview.setWorkCount(stat != null && stat.getWorkCount() != null ? stat.getWorkCount() : 0);
		overview.setFollowerCount(stat != null && stat.getFollowerCount() != null ? stat.getFollowerCount() : 0);
		overview.setIntro(profile != null ? profile.getIntro() : null);
		overview.setJoinedAt(formatYearMonth(account.getSetTime()));

		List<CreatorAccountDetailResp.Badge> badges = new ArrayList<>();
		if (profile != null && CreatorIdentityTypeEnums.STUDIO.getCode() == profile.getIdentityType()) {
			badges.add(mkBadge("team", "团队主体"));
		} else if (profile != null && CreatorIdentityTypeEnums.ORG.getCode() == profile.getIdentityType()) {
			badges.add(mkBadge("org", "公司主体"));
		}
		if (profile != null && CreatorProfileAuditStatusEnums.APPROVED.getCode() == profile.getAuditStatus()) {
			badges.add(mkBadge("realName", "实名认证"));
		}
		overview.setBadges(badges);
		resp.setOverview(overview);

		// baseInfo
		CreatorAccountDetailResp.BaseInfo base = new CreatorAccountDetailResp.BaseInfo();
		base.setNickname(account.getNickname());
		Integer identityType = profile != null ? profile.getIdentityType() : CreatorIdentityTypeEnums.PERSONAL.getCode();
		base.setIdentityType(identityType);
		CreatorIdentityTypeEnums identityEnum = CreatorIdentityTypeEnums.fromCode(identityType);
		base.setIdentityTypeText(identityEnum != null ? identityEnum.getLabel() : "");
		base.setIdentityTypeOptions(identityOptions());
		base.setCity(profile != null ? profile.getCity() : "");
		base.setExpertise(profile != null ? profile.getExpertise() : "");
		base.setIntro(profile != null ? profile.getIntro() : "");
		resp.setBaseInfo(base);

		// contacts
		List<CreatorAccountDetailResp.ContactItem> contacts = new ArrayList<>();
		contacts.add(mkContact("mobile", "手机号", account.getMobileNumber() != null,
				maskMobile(account.getMobilePrefix(), account.getMobileNumber()), "更换手机"));
		contacts.add(mkContact("email", "邮箱", account.getUserAccount() != null,
				maskEmail(account.getUserAccount()), "更换邮箱"));
		CreatorSocialBindEntity wechat = creatorSocialBindDao.findByCreatorAndProvider(account.getId(), "wechat");
		contacts.add(mkContact("wechat", "微信号", wechat != null,
				wechat != null ? wechat.getNickname() : "绑定微信可接收合作消息通知",
				wechat != null ? "解绑" : "去绑定"));
		resp.setContacts(contacts);

		// identities：4 类，查已有记录 + 补默认未认证
		List<CreatorIdentityEntity> existing = creatorIdentityDao.findByCreatorId(account.getId());
		Map<Integer, CreatorIdentityEntity> map = new HashMap<>();
		for (CreatorIdentityEntity e : existing) {
			map.put(e.getIdentityType(), e);
		}
		List<CreatorAccountDetailResp.IdentityItem> identities = new ArrayList<>();
		for (Integer type : IDENTITY_TYPES) {
			CreatorAccountDetailResp.IdentityItem item = new CreatorAccountDetailResp.IdentityItem();
			item.setIdentityType(type);
			item.setName(IDENTITY_NAMES[type]);
			item.setIcon(identityIcon(type));
			CreatorIdentityEntity e = map.get(type);
			if (e == null) {
				item.setStatus(CreatorIdentityAuditStatusEnums.PENDING.getCode());
				item.setStatusText(CreatorIdentityAuditStatusEnums.PENDING.getLabel());
				item.setEnabled(true);
			} else {
				item.setStatus(e.getAuditStatus());
				item.setStatusText(statusText(e.getAuditStatus()));
				item.setEnabled(true);
				if (e.getPassTime() != null) {
					item.setPassTime(FMT_DAYS.format(LocalDateTime.ofInstant(e.getPassTime().toInstant(), ZoneId.systemDefault())));
				}
			}
			identities.add(item);
		}
		resp.setIdentities(identities);

		// security
		CreatorAccountDetailResp.Security sec = new CreatorAccountDetailResp.Security();
		sec.setHasPassword(StringUtils.isNotEmpty(account.getUserPassword()));
		if (apply != null) {
			sec.setCancelApplying(true);
			CreatorAccountDetailResp.CancelApplyStatus cs = new CreatorAccountDetailResp.CancelApplyStatus();
			cs.setStatus(apply.getStatus());
			cs.setApplyTime(formatTime(apply.getApplyTime()));
			cs.setCancelDeadline(formatTime(apply.getCancelDeadline()));
			cs.setCanRevoke(CreatorCancelStatusEnums.COOL_DOWN.getCode() == apply.getStatus());
			cs.setBlockReasons(Collections.emptyList());
			sec.setCancelApplyStatus(cs);
		} else {
			sec.setCancelApplying(false);
		}
		resp.setSecurity(sec);

		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase changeEmail(@Valid @RequestBody CreatorEmailChangeQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (!PasswordHashUtils.matches(query.getPassword(), account.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("old_password_error"));
		}
		String newEmail = query.getNewEmail().trim().toLowerCase();
		if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
			return setResultError(I18nUtil.getMessage("creator.email_invalid"));
		}
		String cacheKey = RedisKeyConstants.CREATOR_EMAIL_CODE_KEY + newEmail;
		Object code = redisUtil.get(cacheKey);
		if (code == null || !code.toString().equals(query.getEmailCode())) {
			return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
		}
		if (sysUserDao.findCreatorByAcctive(newEmail) != null) {
			return setResultError(I18nUtil.getMessage("user.account_exist"));
		}
		Date now = new Date();
		account.setUserAccount(newEmail);
		account.setGmtModified(now);
		try {
			creatorAccountDao.updateById(account);
			if (account.getSysUserId() != null) {
				sysUserDao.updateCreatorEmail(account.getSysUserId(), newEmail);
			}
		} catch (Exception e) {
			log.error("changeEmail creatorId={}", account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		redisUtil.del(cacheKey);
		CreatorTokenUtil.invalidateSession(account.getUserAccount());
		log.info("creator changeEmail creatorId={}", account.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase changeMobile(@Valid @RequestBody CreatorMobileChangeQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		String cacheKey = RedisKeyConstants.SMS_CODE_KEY + query.getMobilePrefix() + query.getMobileNumber();
		Object code = redisUtil.get(cacheKey);
		if (code == null || !code.toString().equals(query.getCode())) {
			return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
		}
		int count = creatorAccountDao.countByMobile(query.getMobilePrefix(), query.getMobileNumber());
		if (count > 0 && !Objects.equals(account.getMobilePrefix(), query.getMobilePrefix())
				&& !Objects.equals(account.getMobileNumber(), query.getMobileNumber())) {
			return setResultError(I18nUtil.getMessage("user.mobile_exist"));
		}
		Date now = new Date();
		account.setMobilePrefix(query.getMobilePrefix());
		account.setMobileNumber(query.getMobileNumber());
		account.setGmtModified(now);
		try {
			creatorAccountDao.updateById(account);
			if (account.getSysUserId() != null) {
				sysUserDao.updateCreatorMobile(account.getSysUserId(), query.getMobilePrefix(), query.getMobileNumber());
			}
		} catch (Exception e) {
			log.error("changeMobile creatorId={}", account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		redisUtil.del(cacheKey);
		log.info("creator changeMobile creatorId={}", account.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase wechatQrcode(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		String scene = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
		String qrUrl = "weixin://bind?scene=" + scene; // TODO 替换真实微信开放平台 URL
		String redisKey = RedisKeyConstants.CREATOR_WECHAT_BIND_SCENE + scene;
		redisUtil.set(redisKey + ":creatorId", account.getId().toString(), 300L);
		CreatorWechatBindResp resp = new CreatorWechatBindResp();
		resp.setScene(scene);
		resp.setQrCodeUrl(qrUrl);
		resp.setExpireSeconds(300L);
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase wechatStatus(@RequestParam("scene") String scene, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		String bindKey = RedisKeyConstants.CREATOR_WECHAT_BIND_SCENE + scene + ":bound";
		Object value = redisUtil.get(bindKey);
		if (value == null) {
			String creatorKey = RedisKeyConstants.CREATOR_WECHAT_BIND_SCENE + scene + ":creatorId";
			if (redisUtil.get(creatorKey) == null) {
				return setResultSuccess(-1); // 已过期
			}
			return setResultSuccess(0); // 待扫码
		}
		// 已绑定，落库
		String[] parts = value.toString().split("\\|", 3);
		CreatorSocialBindEntity entity = new CreatorSocialBindEntity();
		entity.setCreatorId(account.getId());
		entity.setProvider("wechat");
		entity.setOpenId(parts[0]);
		entity.setUnionId(parts.length > 1 ? parts[1] : "");
		entity.setNickname(parts.length > 2 ? parts[2] : "");
		entity.setBindTime(new Date());
		entity.setSetTime(new Date());
		entity.setGmtModified(new Date());
		try {
			CreatorSocialBindEntity exist = creatorSocialBindDao.findByCreatorAndProvider(account.getId(), "wechat");
			if (exist != null) {
				entity.setId(exist.getId());
				creatorSocialBindDao.updateById(entity);
			} else {
				creatorSocialBindDao.insert(entity);
			}
			redisUtil.del(RedisKeyConstants.CREATOR_WECHAT_BIND_SCENE + scene);
		} catch (DuplicateKeyException e) {
			log.warn("wechat bind duplicate creatorId={}", account.getId());
			return setResultSuccess(2);
		}
		return setResultSuccess(2);
	}

	@Override
	public ResponseBase wechatUnbind(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorSocialBindEntity entity = creatorSocialBindDao.findByCreatorAndProvider(account.getId(), "wechat");
		if (entity == null) {
			return setResultError(I18nUtil.getMessage("not_binded"));
		}
		creatorSocialBindDao.deleteById(entity.getId());
		log.info("creator wechat unbind creatorId={}", account.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase identities(HttpServletRequest request) {
		return detail(request);
	}

	@Override
	public ResponseBase identityDetail(@RequestParam("identityType") Integer identityType, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorIdentityEntity e = creatorIdentityDao.findByCreatorAndType(account.getId(), identityType);
		if (e == null) {
			return setResultError(I18nUtil.getMessage("record_not_exist"));
		}
		CreatorIdentityDetailResp resp = new CreatorIdentityDetailResp();
		resp.setIdentityType(e.getIdentityType());
		resp.setStatus(e.getAuditStatus());
		resp.setStatusText(statusText(e.getAuditStatus()));
		resp.setRejectReason(e.getRejectReason());
		resp.setRealName(e.getRealName());
		resp.setIdCardMask(maskIdCard2(e.getIdCardNoCipher(), safeAesKey()));
		resp.setIdCardFrontUrl(mediaUrlService.sign(e.getIdCardFront()));
		resp.setIdCardBackUrl(mediaUrlService.sign(e.getIdCardBack()));
		resp.setOrgName(e.getOrgName());
		resp.setOrgLicenseUrl(mediaUrlService.sign(e.getOrgLicense()));
		resp.setMaterials(e.getMaterials());
		resp.setApplyTime(e.getSetTime());
		resp.setAuditTime(e.getAuditTime());
		resp.setPassTime(e.getPassTime());
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase applyIdentity(@Valid @RequestBody CreatorIdentityApplyQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (!IDENTITY_TYPES.contains(query.getIdentityType())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		Date now = new Date();
		CreatorIdentityEntity existing = creatorIdentityDao.findByCreatorAndType(account.getId(), query.getIdentityType());
		if (existing != null && CreatorIdentityAuditStatusEnums.REVIEWING.getCode() == existing.getAuditStatus()) {
			return setResultError(I18nUtil.getMessage("creator.identity_reviewing"));
		}
		CreatorIdentityEntity entity = new CreatorIdentityEntity();
		entity.setCreatorId(account.getId());
		entity.setIdentityType(query.getIdentityType());
		entity.setRealName(query.getRealName());
		if (StringUtils.isNotEmpty(query.getIdCardNo())) {
			try {
				entity.setIdCardNoCipher(AesUtils.encrypt(query.getIdCardNo(), safeAesKey()));
				entity.setIdCardHash(sha256(query.getIdCardNo()));
			} catch (Exception ex) {
				log.error("encrypt idCard failed", ex);
				throw new BaseException(I18nUtil.getMessage("base_error"), ex);
			}
		}
		entity.setIdCardFront(query.getIdCardFront());
		entity.setIdCardBack(query.getIdCardBack());
		entity.setOrgName(query.getOrgName());
		entity.setOrgLicense(query.getOrgLicense());
		entity.setMaterials(query.getMaterials());
		entity.setRemark(query.getRemark());
		entity.setAuditStatus(CreatorIdentityAuditStatusEnums.REVIEWING.getCode());
		entity.setSetTime(now);
		entity.setGmtModified(now);
		try {
			if (existing != null) {
				entity.setId(existing.getId());
				creatorIdentityDao.updateById(entity);
			} else {
				creatorIdentityDao.insert(entity);
			}
		} catch (Exception e) {
			log.error("applyIdentity creatorId={}", account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("creator applyIdentity creatorId={} type={}", account.getId(), query.getIdentityType());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase setting(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorSettingResp resp = new CreatorSettingResp();
		CreatorSettingEntity s = creatorSettingDao.findByCreatorId(account.getId());
		int pProfile = s == null || s.getProfileVisible() == null ? 1 : s.getProfileVisible();
		int pContact = s == null || s.getContactVisible() == null ? 1 : s.getContactVisible();
		int pWork = s == null || s.getWorkVisible() == null ? 1 : s.getWorkVisible();
		int pSys = s == null || s.getPushSystem() == null ? 1 : s.getPushSystem();
		int pCoop = s == null || s.getPushCoop() == null ? 1 : s.getPushCoop();
		int pAct = s == null || s.getPushActivity() == null ? 0 : s.getPushActivity();
		int pAudit = s == null || s.getPushAudit() == null ? 1 : s.getPushAudit();
		int pInc = s == null || s.getPushIncome() == null ? 1 : s.getPushIncome();
		CreatorSettingResp.Privacy privacy = new CreatorSettingResp.Privacy();
		privacy.setProfileVisible(pProfile);
		privacy.setContactVisible(pContact);
		privacy.setWorkVisible(pWork);
		CreatorSettingResp.Push push = new CreatorSettingResp.Push();
		push.setSystemNotice(pSys);
		push.setCoopMessage(pCoop);
		push.setActivityPush(pAct);
		push.setAuditMessage(pAudit);
		push.setIncomeNotice(pInc);
		resp.setPrivacy(privacy);
		resp.setPush(push);
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase saveSetting(@Valid @RequestBody CreatorSettingSaveQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		Date now = new Date();
		CreatorSettingEntity s = creatorSettingDao.findByCreatorId(account.getId());
		if (s == null) {
			s = new CreatorSettingEntity();
			s.setCreatorId(account.getId());
			s.setProfileVisible(1);
			s.setContactVisible(1);
			s.setWorkVisible(1);
			s.setPushSystem(1);
			s.setPushCoop(1);
			s.setPushActivity(0);
			s.setPushAudit(1);
			s.setPushIncome(1);
			s.setSetTime(now);
		}
		if (query.getProfileVisible() != null) {
			s.setProfileVisible(query.getProfileVisible());
		}
		if (query.getContactVisible() != null) {
			s.setContactVisible(query.getContactVisible());
		}
		if (query.getWorkVisible() != null) {
			s.setWorkVisible(query.getWorkVisible());
		}
		if (query.getPushSystem() != null) {
			s.setPushSystem(query.getPushSystem());
		}
		if (query.getPushCoop() != null) {
			s.setPushCoop(query.getPushCoop());
		}
		if (query.getPushActivity() != null) {
			s.setPushActivity(query.getPushActivity());
		}
		if (query.getPushAudit() != null) {
			s.setPushAudit(query.getPushAudit());
		}
		if (query.getPushIncome() != null) {
			s.setPushIncome(query.getPushIncome());
		}
		s.setGmtModified(now);
		try {
			if (s.getId() == null) {
				creatorSettingDao.insert(s);
			} else {
				creatorSettingDao.updateById(s);
			}
		} catch (Exception e) {
			log.error("saveSetting creatorId={}", account.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase cancelStatus(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorAccountDetailResp.Security sec = new CreatorAccountDetailResp.Security();
		sec.setHasPassword(StringUtils.isNotEmpty(account.getUserPassword()));
		CreatorCancelApplyEntity apply = creatorCancelApplyDao.findLatestByCreator(account.getId());
		if (apply != null) {
			sec.setCancelApplying(true);
			CreatorAccountDetailResp.CancelApplyStatus cs = new CreatorAccountDetailResp.CancelApplyStatus();
			cs.setStatus(apply.getStatus());
			cs.setApplyTime(formatTime(apply.getApplyTime()));
			cs.setCancelDeadline(formatTime(apply.getCancelDeadline()));
			cs.setCanRevoke(CreatorCancelStatusEnums.COOL_DOWN.getCode() == apply.getStatus());
			cs.setBlockReasons(blockReasons(account));
			sec.setCancelApplyStatus(cs);
		} else {
			sec.setCancelApplying(false);
		}
		return setResultSuccess(sec);
	}

	@Override
	public ResponseBase applyCancel(@Valid @RequestBody CreatorCancelApplyQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (!PasswordHashUtils.matches(query.getPassword(), account.getUserPassword())) {
			return setResultError(I18nUtil.getMessage("old_password_error"));
		}
		List<String> blocks = blockReasons(account);
		if (!blocks.isEmpty()) {
			return setResultError(blocks.get(0));
		}
		CreatorCancelApplyEntity exists = creatorCancelApplyDao.findLatestByCreator(account.getId());
		if (exists != null && CreatorCancelStatusEnums.COOL_DOWN.getCode() == exists.getStatus()) {
			return setResultError(I18nUtil.getMessage("creator.cancel_applying"));
		}
		Date now = new Date();
		CreatorCancelApplyEntity apply = new CreatorCancelApplyEntity();
		apply.setCreatorId(account.getId());
		apply.setReason(query.getReason());
		apply.setStatus(CreatorCancelStatusEnums.COOL_DOWN.getCode());
		apply.setApplyTime(now);
		apply.setCancelDeadline(Date.from(Instant.ofEpochMilli(now.getTime()).plusSeconds(COOL_DOWN_DAYS * 86400L)));
		apply.setSetTime(now);
		apply.setGmtModified(now);
		creatorCancelApplyDao.insert(apply);
		log.info("creator applyCancel creatorId={}", account.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase revokeCancel(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorCancelApplyEntity apply = creatorCancelApplyDao.findLatestByCreator(account.getId());
		if (apply == null) {
			return setResultError(I18nUtil.getMessage("record_not_exist"));
		}
		if (CreatorCancelStatusEnums.COOL_DOWN.getCode() != apply.getStatus()) {
			return setResultError(I18nUtil.getMessage("creator.cancel_not_revocable"));
		}
		apply.setStatus(CreatorCancelStatusEnums.REVOKED.getCode());
		apply.setHandleTime(new Date());
		apply.setGmtModified(new Date());
		creatorCancelApplyDao.updateById(apply);
		log.info("creator revokeCancel creatorId={}", account.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}


	private String safeAesKey() {
		if (StringUtils.isEmpty(identityAesKey)) {
			throw new IllegalStateException("creator.identity.aes-key 未配置");
		}
		return identityAesKey;
	}

	private static String sha256(String text) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : bytes) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new BaseException("sha256 failed", e);
		}
	}

	private static String maskMobile(String prefix, String number) {
		if (number == null) {
			return "";
		}
		String masked = number.length() <= 7 ? number : number.substring(0, 3) + "****" + number.substring(number.length() - 4);
		return (prefix == null ? "" : prefix) + masked;
	}

	private static String maskEmail(String email) {
		if (email == null || !email.contains("@")) {
			return "";
		}
		int at = email.indexOf('@');
		String name = email.substring(0, at);
		if (name.length() <= 1) {
			return "*" + email.substring(at);
		}
		return name.charAt(0) + "****" + email.substring(at);
	}

	private static String maskIdCard2(String idCardNoCipher, String aesKey) {
		if (StringUtils.isEmpty(idCardNoCipher)) {
			return "";
		}
		try {
			String plain = AesUtils.decrypt(idCardNoCipher, aesKey);
			return plain.length() > 8 ? plain.substring(0, 4) + "********" + plain.substring(plain.length() - 4) : "********";
		} catch (Exception e) {
			return "********";
		}
	}

	private static String formatTime(Date time) {
		if (time == null) {
			return null;
		}
		return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).format(FMT_FULL);
	}

	private static String formatYearMonth(Date time) {
		if (time == null) {
			return null;
		}
		return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM"));
	}

	private static CreatorAccountDetailResp.Badge mkBadge(String key, String label) {
		CreatorAccountDetailResp.Badge b = new CreatorAccountDetailResp.Badge();
		b.setKey(key);
		b.setLabel(label);
		return b;
	}

	private static CreatorAccountDetailResp.ContactItem mkContact(String key, String label, boolean bound, String value, String action) {
		CreatorAccountDetailResp.ContactItem c = new CreatorAccountDetailResp.ContactItem();
		c.setKey(key);
		c.setLabel(label);
		c.setBound(bound);
		c.setValue(value);
		c.setAction(action);
		return c;
	}

	private static List<CreatorAccountDetailResp.IdentityOption> identityOptions() {
		List<CreatorAccountDetailResp.IdentityOption> opts = new ArrayList<>();
		opts.add(mkOpt("个人创作者", 1));
		opts.add(mkOpt("公司机构", 2));
		opts.add(mkOpt("工作室团队", 3));
		return opts;
	}

	private static CreatorAccountDetailResp.IdentityOption mkOpt(String label, int value) {
		CreatorAccountDetailResp.IdentityOption o = new CreatorAccountDetailResp.IdentityOption();
		o.setLabel(label);
		o.setValue(value);
		return o;
	}

	private static String identityIcon(int type) {
		switch (type) {
			case 1: return "writer";
			case 2: return "actor";
			case 3: return "director";
			case 4: return "org";
			default: return "user";
		}
	}

	private static String statusText(int status) {
		switch (status) {
			case 0: return CreatorIdentityAuditStatusEnums.PENDING.getLabel();
			case 1: return CreatorIdentityAuditStatusEnums.REVIEWING.getLabel();
			case 2: return CreatorIdentityAuditStatusEnums.PASS.getLabel();
			case 3: return CreatorIdentityAuditStatusEnums.REJECT.getLabel();
			default: return "";
		}
	}

	private static List<String> blockReasons(CreatorAccountEntity account) {
		List<String> reasons = new ArrayList<>();
		long availableCoins = (account.getCoinBalance() == null ? 0L : account.getCoinBalance())
				- (account.getFrozenCoinBalance() == null ? 0L : account.getFrozenCoinBalance());
		if (availableCoins > 0) {
			reasons.add("钱包有可用余额，请先提现或放弃");
		}
		return reasons;
	}
}
