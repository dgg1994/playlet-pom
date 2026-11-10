package com.playlet.internal.service.creator.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.api.response.CreatorCertificationDetailResp;
import com.playlet.internal.api.response.CreatorCertificationOptionsResp;
import com.playlet.internal.api.response.CreatorCertificationRecordResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.dao.creator.CreatorCertificationDao;
import com.playlet.internal.dao.creator.CreatorCertificationRecordDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorCertificationEntity;
import com.playlet.internal.entity.creator.CreatorCertificationRecordEntity;
import com.playlet.internal.enums.CreatorCertificationCategoryEnums;
import com.playlet.internal.enums.CreatorCertificationRecordStatusEnums;
import com.playlet.internal.enums.CreatorIdentityAuditStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.query.creator.CreatorCertificationSaveQuery;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.service.creator.CreatorCertificationService;
import com.playlet.internal.utils.AesUtils;
import com.playlet.internal.utils.CreatorTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 认证管理（作家端）。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorCertificationServiceImpl extends BaseApiService implements CreatorCertificationService {

	private static final DateTimeFormatter FMT_MINUTE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final ObjectMapper MAPPER = new ObjectMapper();

	// 认证身份（类别内编码 1-4），与原型图卡片一一对应
	private static final String[] PERSONAL_IDENTITY_NAMES = {"", "编剧", "演员", "导演", "版权人"};
	private static final String[] PERSONAL_IDENTITY_DESCS = {"",
			"可投稿剧本、接受IP改编，剧本按分成获得收益",
			"获取短剧合作机会，出演获得报酬收益",
			"承接短剧拍摄指导，查看排名短剧对应收入",
			"以个人身份与平台开展版权合作，获得收益"};
	private static final String[] ORG_IDENTITY_NAMES = {"", "编剧机构", "演员机构", "导演机构", "版权机构"};
	private static final String[] ORG_IDENTITY_DESCS = {"",
			"可邀请编剧，管理编剧团队成员，查看成员创作数据",
			"可邀请演员、管理演员，承接短剧项目合作",
			"承接短剧拍摄与指导，获得分成收益",
			"与平台版权合作，支持多名成员协作"};

	private static final List<String> GENRE_TYPES = Arrays.asList("实拍", "AIGC", "动画", "混合制作");
	private static final List<String> MARKETS = Arrays.asList("国内", "海外", "国内外");
	private static final List<String> PERSONAL_FIELDS = Arrays.asList("剧本精修", "原创定制", "IP改编", "单集定制");
	private static final List<String> SERVICES = Arrays.asList("原创", "定制", "IP改编", "制作");

	@Autowired
	private CreatorCertificationDao creatorCertificationDao;
	@Autowired
	private CreatorCertificationRecordDao creatorCertificationRecordDao;
	@Autowired
	private MediaUrlService mediaUrlService;

	@Value("${creator.identity.aes-key:${script.contact.aes-key:}}")
	private String identityAesKey;

	@Override
	public ResponseBase options(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorCertificationOptionsResp resp = new CreatorCertificationOptionsResp();
		resp.setPersonalIdentities(identityOptions(PERSONAL_IDENTITY_NAMES, PERSONAL_IDENTITY_DESCS));
		resp.setOrgIdentities(identityOptions(ORG_IDENTITY_NAMES, ORG_IDENTITY_DESCS));
		resp.setGenreTypes(GENRE_TYPES);
		resp.setMarkets(MARKETS);
		resp.setPersonalFields(PERSONAL_FIELDS);
		resp.setServices(SERVICES);
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase detail(@RequestParam("category") Integer category, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		if (CreatorCertificationCategoryEnums.fromCode(category) == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		CreatorCertificationDetailResp resp = new CreatorCertificationDetailResp();
		resp.setCategory(category);
		resp.setIdentityTypes(new ArrayList<>());
		resp.setServices(new ArrayList<>());
		resp.setWorks(new ArrayList<>());
		CreatorCertificationEntity e = creatorCertificationDao.findByCreatorAndCategory(account.getId(), category);
		if (e == null) {
			resp.setSubmitStatus(0);
			resp.setAuditStatus(CreatorIdentityAuditStatusEnums.PENDING.getCode());
			resp.setAuditStatusText(CreatorIdentityAuditStatusEnums.PENDING.getLabel());
			return setResultSuccess(resp);
		}
		resp.setSubmitStatus(e.getSubmitStatus());
		resp.setAuditStatus(e.getAuditStatus());
		resp.setAuditStatusText(auditStatusText(e.getAuditStatus()));
		resp.setRejectReason(e.getRejectReason());
		resp.setSubmitTime(e.getSubmitTime());
		resp.setAuditTime(e.getAuditTime());
		resp.setIdentityTypes(parseIntList(e.getIdentityTypes()));
		resp.setRealName(e.getRealName());
		resp.setIdCardMask(maskIdCard(e.getIdCardNoCipher()));
		resp.setIdCardFront(e.getIdCardFront());
		resp.setIdCardFrontUrl(mediaUrlService.sign(e.getIdCardFront()));
		resp.setIdCardBack(e.getIdCardBack());
		resp.setIdCardBackUrl(mediaUrlService.sign(e.getIdCardBack()));
		resp.setOrgName(e.getOrgName());
		resp.setOrgLicense(e.getOrgLicense());
		resp.setOrgLicenseUrl(mediaUrlService.sign(e.getOrgLicense()));
		resp.setTeamSize(e.getTeamSize());
		resp.setCity(e.getCity());
		resp.setPhone(e.getPhone());
		resp.setWechat(e.getWechat());
		resp.setEmail(e.getEmail());
		resp.setGenreType(e.getGenreType());
		resp.setMarket(e.getMarket());
		resp.setField(e.getField());
		resp.setServices(parseStrList(e.getServices()));
		resp.setPrice(e.getPrice());
		resp.setExtra(e.getExtra());
		resp.setWorks(parseWorks(e.getWorks()));
		return setResultSuccess(resp);
	}

	@Override
	public ResponseBase save(@Valid @RequestBody CreatorCertificationSaveQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorCertificationEntity existing = checkEditable(account.getId(), query.getCategory());
		if (existing == null && CreatorCertificationCategoryEnums.fromCode(query.getCategory()) == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		upsert(account.getId(), query, existing, false);
		log.info("creator certification saveDraft creatorId={} category={}", account.getId(), query.getCategory());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase submit(@Valid @RequestBody CreatorCertificationSaveQuery query, HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		CreatorCertificationEntity existing = checkEditable(account.getId(), query.getCategory());
		if (existing == null && CreatorCertificationCategoryEnums.fromCode(query.getCategory()) == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		// 提交必填校验
		if (query.getIdentityTypes() == null || query.getIdentityTypes().isEmpty()) {
			return setResultError(I18nUtil.getMessage("creator.certification_identity_required"));
		}
		boolean personal = CreatorCertificationCategoryEnums.PERSONAL.getCode() == query.getCategory();
		if (personal) {
			boolean hasCipher = StringUtils.isNotEmpty(query.getIdCardNo()) && !query.getIdCardNo().contains("*");
			boolean keepOld = existing != null && StringUtils.isNotEmpty(existing.getIdCardNoCipher());
			if (StringUtils.isEmpty(query.getRealName())) {
				return setResultError(I18nUtil.getMessage("creator.certification_realname_required"));
			}
			if (!hasCipher && !keepOld) {
				return setResultError(I18nUtil.getMessage("creator.certification_idcard_required"));
			}
		} else {
			if (StringUtils.isEmpty(query.getOrgName())
					|| (StringUtils.isEmpty(query.getOrgLicense())
							&& (existing == null || StringUtils.isEmpty(existing.getOrgLicense())))) {
				return setResultError(I18nUtil.getMessage("creator.certification_org_required"));
			}
		}
		CreatorCertificationEntity entity = upsert(account.getId(), query, existing, true);
		// 时间线：同类别旧的「审核中」置为「已处理」，再插入新提交记录
		creatorCertificationRecordDao.closeReviewing(account.getId(), query.getCategory());
		CreatorCertificationRecordEntity record = new CreatorCertificationRecordEntity();
		record.setCreatorId(account.getId());
		record.setCategory(query.getCategory());
		CreatorCertificationCategoryEnums categoryEnum = CreatorCertificationCategoryEnums.fromCode(query.getCategory());
		record.setTitle(categoryEnum.getLabel() + "认证申请提交，等待平台审核");
		record.setStatus(CreatorCertificationRecordStatusEnums.REVIEWING.getCode());
		Date now = entity.getGmtModified();
		record.setSetTime(now);
		record.setGmtModified(now);
		creatorCertificationRecordDao.insert(record);
		log.info("creator certification submit creatorId={} category={}", account.getId(), query.getCategory());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase records(HttpServletRequest request) {
		CreatorAccountEntity account = CreatorTokenUtil.resolveAccount(request);
		if (account == null) {
			return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
		}
		List<CreatorCertificationRecordEntity> list = creatorCertificationRecordDao.findByCreatorId(account.getId());
		List<CreatorCertificationRecordResp> resp = new ArrayList<>();
		for (CreatorCertificationRecordEntity e : list) {
			CreatorCertificationRecordResp item = new CreatorCertificationRecordResp();
			item.setId(e.getId());
			item.setTime(e.getSetTime() == null ? "" :
					LocalDateTime.ofInstant(e.getSetTime().toInstant(), ZoneId.systemDefault()).format(FMT_MINUTE));
			item.setText(e.getTitle());
			item.setStatus(e.getStatus());
			item.setStatusText(CreatorCertificationRecordStatusEnums.labelOf(e.getStatus()));
			resp.add(item);
		}
		return setResultSuccess(resp);
	}

	/** 审核中的申请不允许修改；返回已有记录（无则 null），类别非法返回 null 且由调用方判错 */
	private CreatorCertificationEntity checkEditable(Integer creatorId, Integer category) {
		if (CreatorCertificationCategoryEnums.fromCode(category) == null) {
			return null;
		}
		CreatorCertificationEntity existing = creatorCertificationDao.findByCreatorAndCategory(creatorId, category);
		if (existing != null && CreatorIdentityAuditStatusEnums.REVIEWING.getCode() == existing.getAuditStatus()) {
			throw new BaseException(I18nUtil.getMessage("creator.certification_reviewing"));
		}
		return existing;
	}

	private CreatorCertificationEntity upsert(Integer creatorId, CreatorCertificationSaveQuery query,
											  CreatorCertificationEntity existing, boolean submit) {
		Date now = new Date();
		CreatorCertificationEntity entity = existing != null ? existing : new CreatorCertificationEntity();
		entity.setCreatorId(creatorId);
		entity.setCategory(query.getCategory());
		entity.setIdentityTypes(writeJson(query.getIdentityTypes()));
		entity.setRealName(trim(query.getRealName()));
		// 证件号：明文才重新加密，掩码回填保留原密文
		if (StringUtils.isNotEmpty(query.getIdCardNo()) && !query.getIdCardNo().contains("*")) {
			try {
				entity.setIdCardNoCipher(AesUtils.encrypt(query.getIdCardNo(), safeAesKey()));
				entity.setIdCardHash(sha256(query.getIdCardNo()));
			} catch (Exception ex) {
				log.error("certification encrypt idCard failed", ex);
				throw new BaseException(I18nUtil.getMessage("base_error"), ex);
			}
		} else if (existing == null) {
			entity.setIdCardNoCipher("");
			entity.setIdCardHash("");
		}
		entity.setIdCardFront(trim(query.getIdCardFront()));
		entity.setIdCardBack(trim(query.getIdCardBack()));
		entity.setOrgName(trim(query.getOrgName()));
		entity.setOrgLicense(trim(query.getOrgLicense()));
		entity.setTeamSize(trim(query.getTeamSize()));
		entity.setCity(trim(query.getCity()));
		entity.setPhone(trim(query.getPhone()));
		entity.setWechat(trim(query.getWechat()));
		entity.setEmail(trim(query.getEmail()));
		entity.setGenreType(trim(query.getGenreType()));
		entity.setMarket(trim(query.getMarket()));
		entity.setField(trim(query.getField()));
		entity.setServices(writeJson(query.getServices()));
		entity.setPrice(trim(query.getPrice()));
		entity.setExtra(trim(query.getExtra()));
		entity.setWorks(writeJson(query.getWorks()));
		if (submit) {
			entity.setSubmitStatus(1);
			entity.setAuditStatus(CreatorIdentityAuditStatusEnums.REVIEWING.getCode());
			entity.setRejectReason("");
			entity.setSubmitTime(now);
		} else if (existing == null) {
			entity.setSubmitStatus(0);
			entity.setAuditStatus(CreatorIdentityAuditStatusEnums.PENDING.getCode());
		}
		entity.setGmtModified(now);
		try {
			if (entity.getId() == null) {
				entity.setSetTime(now);
				creatorCertificationDao.insert(entity);
			} else {
				creatorCertificationDao.updateById(entity);
			}
		} catch (Exception e) {
			log.error("certification upsert creatorId={} category={}", creatorId, query.getCategory(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return entity;
	}

	private String safeAesKey() {
		if (StringUtils.isEmpty(identityAesKey)) {
			throw new IllegalStateException("creator.identity.aes-key 未配置");
		}
		return identityAesKey;
	}

	private String maskIdCard(String cipher) {
		if (StringUtils.isEmpty(cipher)) {
			return "";
		}
		try {
			String plain = AesUtils.decrypt(cipher, safeAesKey());
			return plain.length() > 8 ? plain.substring(0, 3) + "***********" + plain.substring(plain.length() - 4) : "********";
		} catch (Exception e) {
			return "********";
		}
	}

	private static List<CreatorCertificationOptionsResp.IdentityOption> identityOptions(String[] names, String[] descs) {
		List<CreatorCertificationOptionsResp.IdentityOption> list = new ArrayList<>();
		for (int i = 1; i < names.length; i++) {
			CreatorCertificationOptionsResp.IdentityOption o = new CreatorCertificationOptionsResp.IdentityOption();
			o.setCode(i);
			o.setName(names[i]);
			o.setDesc(descs[i]);
			list.add(o);
		}
		return list;
	}

	private static String auditStatusText(Integer status) {
		if (status == null) {
			return "";
		}
		if (CreatorIdentityAuditStatusEnums.REVIEWING.getCode() == status) {
			return CreatorIdentityAuditStatusEnums.REVIEWING.getLabel();
		}
		if (CreatorIdentityAuditStatusEnums.PASS.getCode() == status) {
			return CreatorIdentityAuditStatusEnums.PASS.getLabel();
		}
		if (CreatorIdentityAuditStatusEnums.REJECT.getCode() == status) {
			return CreatorIdentityAuditStatusEnums.REJECT.getLabel();
		}
		return CreatorIdentityAuditStatusEnums.PENDING.getLabel();
	}

	private static String trim(String s) {
		return s == null ? "" : s.trim();
	}

	private static String writeJson(Object value) {
		if (value == null) {
			return "";
		}
		try {
			return MAPPER.writeValueAsString(value);
		} catch (Exception e) {
			return "";
		}
	}

	private static List<Integer> parseIntList(String json) {
		if (StringUtils.isEmpty(json)) {
			return new ArrayList<>();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<Integer>>() {});
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private static List<String> parseStrList(String json) {
		if (StringUtils.isEmpty(json)) {
			return new ArrayList<>();
		}
		try {
			return MAPPER.readValue(json, new TypeReference<List<String>>() {});
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	private List<CreatorCertificationDetailResp.WorkItem> parseWorks(String json) {
		List<CreatorCertificationDetailResp.WorkItem> list = new ArrayList<>();
		if (StringUtils.isEmpty(json)) {
			return list;
		}
		try {
			List<CreatorCertificationSaveQuery.WorkItem> works =
					MAPPER.readValue(json, new TypeReference<List<CreatorCertificationSaveQuery.WorkItem>>() {});
			for (CreatorCertificationSaveQuery.WorkItem w : works) {
				CreatorCertificationDetailResp.WorkItem item = new CreatorCertificationDetailResp.WorkItem();
				item.setName(w.getName());
				item.setKey(w.getKey());
				item.setUrl(mediaUrlService.sign(w.getKey()));
				list.add(item);
			}
		} catch (Exception e) {
			log.warn("certification parse works failed json={}", json);
		}
		return list;
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
}
