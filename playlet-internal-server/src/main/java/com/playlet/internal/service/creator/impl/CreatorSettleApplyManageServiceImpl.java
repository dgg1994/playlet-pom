package com.playlet.internal.service.creator.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.request.CreatorSettleApplyCreateQuery;
import com.playlet.internal.api.request.CreatorSettleApplyHandleQuery;
import com.playlet.internal.api.response.CreatorSettleApplyDetailResp;
import com.playlet.internal.api.response.CreatorSettleApplyItemEntity;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.creator.CreatorAccountDao;
import com.playlet.internal.dao.creator.CreatorSettleApplyDao;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.creator.CreatorAccountEntity;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import com.playlet.internal.entity.system.SysUserEntity;
import com.playlet.internal.enums.UserStateEnums;
import com.playlet.internal.query.creator.CreatorSettleApplyQuery;
import com.playlet.internal.service.creator.CreatorSettleApplyManageService;
import com.playlet.internal.service.common.MediaUrlService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.PasswordHashUtils;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端入驻申请审核实现
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CreatorSettleApplyManageServiceImpl implements CreatorSettleApplyManageService {

    /** 待查看 */
    public static final int STATUS_PENDING = 0;
    /** 已查看 */
    public static final int STATUS_VIEWED = 1;
    /** 已通过 */
    public static final int STATUS_APPROVED = 2;
    /** 已驳回 */
    public static final int STATUS_REJECTED = 3;
    /** sys_user.user_type：创作者 */
    private static final Integer CREATOR_USER_TYPE = 2;

    @Autowired
    private CreatorSettleApplyDao creatorSettleApplyDao;
    @Autowired
    private CreatorAccountDao creatorAccountDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private MediaUrlService mediaUrlService;

    @Override
    @SysLogAnnotation(module = "入驻审核", type = "POST", remark = "入驻申请列表")
    public ResponseBase findList(@RequestBody(required = false) CreatorSettleApplyQuery query) {
        if (query == null) {
            query = new CreatorSettleApplyQuery();
        }
        if (query.getKeyword() != null) {
            query.setKeyword(query.getKeyword().trim());
        }
        PageHelper.startPage(query.getPageNumber(), query.getPageSize());
        List<CreatorSettleApplyItemEntity> list = creatorSettleApplyDao.findAdminApplyList(query);
        if (list == null) {
            list = new ArrayList<>();
        }
        for (CreatorSettleApplyItemEntity item : list) {
            item.setContactPhone(maskPhone(item.getContactPhone()));
        }
        PageInfo<CreatorSettleApplyItemEntity> page = new PageInfo<>(list);
        return setResultSuccess(page, I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "入驻审核", type = "GET", remark = "入驻申请详情")
    public ResponseBase detail(Integer applyId) {
        if (applyId == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        CreatorProfileEntity profile = creatorSettleApplyDao.selectById(applyId);
        if (profile == null) {
            return setResultError(I18nUtil.getMessage("base_data_null"));
        }
        Integer auditStatus = profile.getAuditStatus() == null ? STATUS_PENDING : profile.getAuditStatus();
        // 打开详情视为已查看
        if (auditStatus == STATUS_PENDING) {
            creatorSettleApplyDao.markViewed(profile.getId(), new Date());
            auditStatus = STATUS_VIEWED;
            profile.setAuditStatus(auditStatus);
        }
        CreatorAccountEntity account = profile.getCreatorId() == null ? null
                : creatorAccountDao.selectById(profile.getCreatorId());
        return setResultSuccess(buildDetail(profile, account), I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "入驻审核", type = "POST", remark = "审核入驻申请")
    public ResponseBase handle(@Valid @RequestBody CreatorSettleApplyHandleQuery query,
            HttpServletRequest request) {
        if (query == null || query.getApplyId() == null || query.getPass() == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        CreatorProfileEntity profile = creatorSettleApplyDao.selectById(query.getApplyId());
        if (profile == null) {
            return setResultError(I18nUtil.getMessage("base_data_null"));
        }
        Integer auditStatus = profile.getAuditStatus() == null ? STATUS_PENDING : profile.getAuditStatus();
        if (auditStatus == STATUS_APPROVED) {
            return setResultError("该申请已通过，无需重复审核");
        }
        Integer adminId = SysUserTokenUtil.resolveAdminId(request);
        Date now = new Date();
        if (Boolean.TRUE.equals(query.getPass())) {
            creatorSettleApplyDao.markApproved(profile.getId(), adminId, now);
            log.info("入驻审核通过 applyId={} adminId={}", profile.getId(), adminId);
        } else {
            if (StringUtils.isEmpty(query.getRejectReason()) || query.getRejectReason().trim().isEmpty()) {
                return setResultError("驳回时请填写驳回原因");
            }
            creatorSettleApplyDao.markRejected(profile.getId(), adminId, query.getRejectReason().trim(), now);
            log.info("入驻审核驳回 applyId={} adminId={}", profile.getId(), adminId);
        }
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "入驻审核", type = "POST", remark = "新增入驻申请")
    public ResponseBase create(@Valid @RequestBody CreatorSettleApplyCreateQuery query,
            HttpServletRequest request) {
        if (query == null) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        String email = normalizeEmail(query.getUserAccount());
        if (!isEmail(email) || StringUtils.isEmpty(query.getUserPassword())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        Integer identityType = query.getIdentityType();
        if (identityType == null || identityType < 1 || identityType > 2) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        Integer creatorRole = query.getCreatorRole() == null ? 3 : query.getCreatorRole();
        if (creatorRole < 1 || creatorRole > 3) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        // 个人认证必填真实姓名；企业/团队必填机构名称
        if (identityType == 1 && StringUtils.isEmpty(query.getRealName())) {
            return setResultError("个人认证请填写真实姓名");
        }
        if (identityType == 2 && StringUtils.isEmpty(query.getOrgName())) {
            return setResultError("团队认证请填写机构名称");
        }
        if (sysUserDao.findCreatorByAcctive(email) != null) {
            return setResultError(I18nUtil.getMessage("user.account_exist"));
        }
        String mobilePrefix = trimToNull(query.getMobilePrefix());
        String mobileNumber = trimToNull(query.getMobileNumber());
        if (mobileNumber != null && creatorAccountDao.countByMobile(mobilePrefix, mobileNumber) > 0) {
            return setResultError("该手机号已被使用");
        }

        Date now = new Date();
        String encodedPwd = PasswordHashUtils.encode(query.getUserPassword());
        // 统一账号：创作者登录身份写入 sys_user（user_type=2）
        SysUserEntity sysUser = new SysUserEntity();
        sysUser.setAcctive(email);
        sysUser.setPassword(encodedPwd);
        sysUser.setUsername(StringUtils.isEmpty(trimToNull(query.getNickname())) ? email : trimToNull(query.getNickname()));
        sysUser.setTel(mobileNumber);
        sysUser.setUserState(UserStateEnums.NORMAL.getIndex());
        sysUser.setUserType(CREATOR_USER_TYPE);
        sysUser.setCreateTime(formatTime(now));
        sysUser.setLoginTime(now);
        sysUserDao.insert(sysUser);

        CreatorAccountEntity account = new CreatorAccountEntity();
        account.setUserAccount(email);
        account.setSysUserId(sysUser.getId());
        account.setUserPassword(encodedPwd);
        account.setMobilePrefix(mobilePrefix);
        account.setMobileNumber(mobileNumber);
        account.setNickname(trimToNull(query.getNickname()));
        account.setUserState(UserStateEnums.NORMAL.getIndex());
        account.setCoinBalance(0L);
        account.setFrozenCoinBalance(0L);
        account.setTotalIncomeCoin(0L);
        account.setSetTime(now);
        account.setGmtModified(now);
        creatorAccountDao.insert(account);

        CreatorProfileEntity profile = new CreatorProfileEntity();
        profile.setCreatorId(account.getId());
        profile.setIdentityType(identityType);
        profile.setCreatorRole(creatorRole);
        profile.setRealName(trimToNull(query.getRealName()));
        profile.setIdCardFront(trimToNull(query.getIdCardFront()));
        profile.setIdCardBack(trimToNull(query.getIdCardBack()));
        profile.setOrgName(trimToNull(query.getOrgName()));
        profile.setOrgLicense(trimToNull(query.getOrgLicense()));
        profile.setBillAddress(trimToNull(query.getBillAddress()));
        profile.setQualifyDesc(trimToNull(query.getQualifyDesc()));
        profile.setAuditStatus(STATUS_PENDING);
        profile.setSetTime(now);
        profile.setGmtModified(now);
        creatorSettleApplyDao.insert(profile);

        log.info("新增入驻申请 creatorId={} applyId={} adminId={} email={}",
                account.getId(), profile.getId(), SysUserTokenUtil.resolveAdminId(request), account.getUserAccount());
        return setResultSuccess(profile.getId(), I18nUtil.getMessage("base_success"));
    }

    /**
     * 组装详情（联系方式返回完整号码，供运营联系）
     */
    private CreatorSettleApplyDetailResp buildDetail(CreatorProfileEntity profile, CreatorAccountEntity account) {
        CreatorSettleApplyDetailResp resp = new CreatorSettleApplyDetailResp();
        resp.setApplyId(profile.getId());
        resp.setApplyNo(buildApplyNo(profile));
        resp.setIdentityType(profile.getIdentityType() == null ? 1 : profile.getIdentityType());
        resp.setCreatorRole(profile.getCreatorRole() == null ? 3 : profile.getCreatorRole());
        resp.setApplyName(resolveApplyName(profile, account));
        resp.setRealName(profile.getRealName());
        resp.setOrgName(profile.getOrgName());
        resp.setIdCardFront(mediaUrlService.sign(profile.getIdCardFront()));
        resp.setIdCardBack(mediaUrlService.sign(profile.getIdCardBack()));
        resp.setOrgLicense(mediaUrlService.sign(profile.getOrgLicense()));
        resp.setBillAddress(profile.getBillAddress());
        resp.setQualifyDesc(profile.getQualifyDesc());
        resp.setApplyTime(profile.getSetTime());
        resp.setAuditStatus(profile.getAuditStatus() == null ? STATUS_PENDING : profile.getAuditStatus());
        resp.setAuditRejectReason(profile.getAuditRejectReason());
        resp.setAuditPassTime(profile.getAuditPassTime());
        if (account != null) {
            resp.setUserAccount(account.getUserAccount());
            resp.setContactName(StringUtils.isEmpty(profile.getRealName()) ? account.getNickname() : profile.getRealName());
            resp.setContactPhone(joinPhone(account));
        } else {
            resp.setContactName(profile.getRealName());
        }
        return resp;
    }

    /**
     * 申请ID：RZ + 申请年份 + 3 位流水号，如 RZ2026001
     */
    private String buildApplyNo(CreatorProfileEntity profile) {
        Date time = profile.getSetTime() == null ? new Date() : profile.getSetTime();
        String year = new SimpleDateFormat("yyyy").format(time);
        return "RZ" + year + String.format("%03d", profile.getId() == null ? 0 : profile.getId());
    }

    private String resolveApplyName(CreatorProfileEntity profile, CreatorAccountEntity account) {
        if (StringUtils.isNotEmpty(profile.getOrgName())) {
            return profile.getOrgName();
        }
        if (StringUtils.isNotEmpty(profile.getRealName())) {
            return profile.getRealName();
        }
        return account == null ? null : account.getNickname();
    }

    private String joinPhone(CreatorAccountEntity account) {
        if (account == null) {
            return null;
        }
        String prefix = account.getMobilePrefix() == null ? "" : account.getMobilePrefix();
        String number = account.getMobileNumber() == null ? "" : account.getMobileNumber();
        String phone = prefix + number;
        return phone.isEmpty() ? null : phone;
    }

    /**
     * 列表手机号脱敏：138****5678
     */
    private String maskPhone(String phone) {
        if (StringUtils.isEmpty(phone)) {
            return phone;
        }
        if (phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static boolean isEmail(String email) {
        return StringUtils.isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String formatTime(Date time) {
        return LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
