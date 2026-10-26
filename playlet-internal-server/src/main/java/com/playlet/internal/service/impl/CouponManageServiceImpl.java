package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.CouponIssueQuery;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.CouponConstants;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.coupon.CouponIssueLogDao;
import com.playlet.internal.dao.coupon.CouponTemplateDao;
import com.playlet.internal.dao.coupon.UserCouponDao;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.coupon.CouponIssueLogEntity;
import com.playlet.internal.entity.coupon.CouponTemplateEntity;
import com.playlet.internal.entity.coupon.UserCouponEntity;
import com.playlet.internal.entity.system.SysUserEntity;
import com.playlet.internal.enums.CouponCurrencyEnums;
import com.playlet.internal.enums.CouponIssueTypeEnums;
import com.playlet.internal.enums.CouponScopeEnums;
import com.playlet.internal.enums.CouponTypeEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.UserCouponStatusEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.CouponManageService;
import com.playlet.internal.service.support.CouponFaceValueSupport;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.SysUserTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端优惠券：模板 CRUD、发放、发放流水。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CouponManageServiceImpl implements CouponManageService {

	@Autowired
	private CouponTemplateDao couponTemplateDao;
	@Autowired
	private UserCouponDao userCouponDao;
	@Autowired
	private CouponIssueLogDao couponIssueLogDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private SysUserDao sysUserDao;
	@Autowired
	private CouponFaceValueSupport couponFaceValueSupport;

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "模板列表")
	public ResponseBase findList(@RequestBody(required = false) CouponTemplateEntity entity) {
		if (entity == null) {
			entity = new CouponTemplateEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<CouponTemplateEntity> list = couponTemplateDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (CouponTemplateEntity row : list) {
			row.setFaceValueText(couponFaceValueSupport.format(row));
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "模板详情")
	public ResponseBase detail(@RequestBody CouponTemplateEntity entity) {
		if (entity == null || entity.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		CouponTemplateEntity row = couponTemplateDao.selectById(entity.getId());
		if (row == null) {
			return setResultError(I18nUtil.getMessage("base_data_null"));
		}
		row.setFaceValueText(couponFaceValueSupport.format(row));
		return setResultSuccess(row, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "新增模板")
	public ResponseBase save(@RequestBody CouponTemplateEntity entity) {
		try {
			String err = validateTemplate(entity, true);
			if (err != null) {
				return setResultError(err);
			}
			normalizeTemplate(entity);
			if (entity.getStatus() == null) {
				entity.setStatus(CouponConstants.TEMPLATE_STATUS_ON);
			}
			if (entity.getPerUserLimit() == null) {
				entity.setPerUserLimit(CouponConstants.DEFAULT_PER_USER_LIMIT);
			}
			entity.setIssuedCount(0);
			entity.setUsedCount(0);
			GenericityUtil.setDate(entity);
			try {
				couponTemplateDao.insert(entity);
			} catch (DuplicateKeyException e) {
				return setResultError("券编码已存在");
			}
			log.info("coupon template save id={} code={}", entity.getId(), entity.getCouponCode());
			return setResultSuccess(entity.getId(), I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("coupon template save failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "编辑模板")
	public ResponseBase update(@RequestBody CouponTemplateEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			CouponTemplateEntity old = couponTemplateDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateTemplate(entity, false);
			if (err != null) {
				return setResultError(err);
			}
			normalizeTemplate(entity);
			// 已发放数不由编辑接口改写
			entity.setIssuedCount(null);
			entity.setUsedCount(null);
			GenericityUtil.updateDate(entity);
			couponTemplateDao.updateById(entity);
			log.info("coupon template update id={}", entity.getId());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("coupon template update failed id={}", entity == null ? null : entity.getId(), e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "启用停用")
	public ResponseBase changeStatus(@RequestBody CouponTemplateEntity entity) {
		try {
			if (entity == null || entity.getId() == null || entity.getStatus() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			boolean valid = PublicEnums.ZERO.getIndex().equals(entity.getStatus())
					|| PublicEnums.ONE.getIndex().equals(entity.getStatus());
			if (!valid) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			CouponTemplateEntity old = couponTemplateDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			CouponTemplateEntity upd = new CouponTemplateEntity();
			upd.setId(old.getId());
			upd.setStatus(entity.getStatus());
			GenericityUtil.updateDate(upd);
			couponTemplateDao.updateById(upd);
			log.info("coupon template changeStatus id={} status={}", old.getId(), entity.getStatus());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("coupon template changeStatus failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "发放优惠券")
	public ResponseBase issue(HttpServletRequest request, @RequestBody CouponIssueQuery query) {
		try {
			if (query == null || query.getTemplateId() == null
					|| query.getUidList() == null || query.getUidList().isEmpty()) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			CouponTemplateEntity template = couponTemplateDao.selectById(query.getTemplateId());
			if (template == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (!Integer.valueOf(CouponConstants.TEMPLATE_STATUS_ON).equals(template.getStatus())) {
				return setResultError("券模板未启用");
			}

			// uid 为 0 表示发放给全部用户
			boolean issueAll = query.getUidList().contains(0);
			Set<Integer> uidSet = new HashSet<>();
			if (issueAll) {
				List<Integer> allUids = appAccountDao.findAllUids();
				if (allUids == null || allUids.isEmpty()) {
					return setResultError("暂无可发放用户");
				}
				uidSet.addAll(allUids);
			} else {
				// 去重 uid
				for (Integer uid : query.getUidList()) {
					if (uid != null && uid > 0) {
						uidSet.add(uid);
					}
				}
				if (uidSet.isEmpty()) {
					return setResultError("请选择有效用户");
				}
			}

			int perUserLimit = template.getPerUserLimit() == null
					? CouponConstants.DEFAULT_PER_USER_LIMIT : template.getPerUserLimit();
			List<Integer> issuedUids = new ArrayList<>();
			List<String> skipReasons = new ArrayList<>();

			String batchNo = CouponConstants.BATCH_NO_PREFIX
					+ System.currentTimeMillis()
					+ UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();

			int limitSkipCount = 0;
			for (Integer uid : uidSet) {
				AppAccountEntity account = appAccountDao.findByUid(uid);
				if (account == null) {
					skipReasons.add("uid=" + uid + " 不存在");
					continue;
				}
				int owned = userCouponDao.countByUidAndTemplate(uid, template.getId());
				if (owned >= perUserLimit) {
					// 全员发放时逐条记录会导致原因列表过大，改为汇总
					if (issueAll) {
						limitSkipCount++;
					} else {
						skipReasons.add("uid=" + uid + " 已达限领");
					}
					continue;
				}
				// 总上限：每发一张前乐观加 1
				boolean checkLimit = template.getTotalLimit() != null;
				int rows = couponTemplateDao.incrIssuedCount(template.getId(), 1, checkLimit);
				if (rows <= 0) {
					skipReasons.add("已达总发放上限，停止继续发放");
					break;
				}
				UserCouponEntity userCoupon = buildUserCoupon(template, uid, batchNo);
				GenericityUtil.setDate(userCoupon);
				userCouponDao.insert(userCoupon);
				issuedUids.add(uid);
			}
			if (issueAll && limitSkipCount > 0) {
				skipReasons.add(limitSkipCount + " 人已达限领");
			}

			Integer adminId = SysUserTokenUtil.resolveAdminId(request);
			String operatorName = resolveOperatorName(adminId);
			CouponIssueLogEntity logEntity = new CouponIssueLogEntity();
			logEntity.setBatchNo(batchNo);
			logEntity.setTemplateId(template.getId());
			logEntity.setCouponCode(template.getCouponCode());
			logEntity.setIssueType(CouponIssueTypeEnums.MANUAL.getCode());
			logEntity.setIssueCount(issuedUids.size());
			logEntity.setTargetDesc(buildTargetDesc(uidSet.size(), issuedUids.size(), skipReasons));
			logEntity.setOperatorId(adminId);
			logEntity.setOperatorName(operatorName);
			logEntity.setRemark(query.getRemark());
			GenericityUtil.setDate(logEntity);
			couponIssueLogDao.insert(logEntity);

			Map<String, Object> data = new HashMap<>(8);
			data.put("batchNo", batchNo);
			data.put("issueCount", issuedUids.size());
			data.put("requestCount", uidSet.size());
			data.put("issueAll", issueAll);
			data.put("skipReasons", skipReasons);
			log.info("coupon issue batchNo={} templateId={} issued={}",
					batchNo, template.getId(), issuedUids.size());
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("coupon issue failed", e);
			throw new BaseException("发放失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "优惠券", type = "POST", remark = "发放流水")
	public ResponseBase issueLogList(@RequestBody(required = false) CouponIssueLogEntity entity) {
		if (entity == null) {
			entity = new CouponIssueLogEntity();
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<CouponIssueLogEntity> list = couponIssueLogDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	private UserCouponEntity buildUserCoupon(CouponTemplateEntity template, Integer uid, String batchNo) {
		UserCouponEntity c = new UserCouponEntity();
		c.setUid(uid);
		c.setTemplateId(template.getId());
		c.setCouponCode(template.getCouponCode());
		c.setCouponName(template.getCouponName());
		c.setCouponType(template.getCouponType());
		c.setDiscountRate(template.getDiscountRate());
		c.setThresholdAmt(template.getThresholdAmt());
		c.setReduceAmt(template.getReduceAmt());
		c.setCurrency(template.getCurrency());
		c.setScope(template.getScope());
		c.setStatus(UserCouponStatusEnums.UNUSED.getIndex());
		c.setIssueBatchNo(batchNo);
		c.setExpireTime(calcExpireTime(template));
		return c;
	}

	private Date calcExpireTime(CouponTemplateEntity template) {
		Date now = new Date();
		if (template.getValidDays() != null && template.getValidDays() > 0) {
			return Date.from(now.toInstant().plus(template.getValidDays(), ChronoUnit.DAYS));
		}
		if (template.getValidEnd() != null) {
			return template.getValidEnd();
		}
		// 未配置有效期：默认 30 天
		return Date.from(now.toInstant().plus(30, ChronoUnit.DAYS));
	}

	private void normalizeTemplate(CouponTemplateEntity entity) {
		if (entity.getCouponCode() != null) {
			entity.setCouponCode(entity.getCouponCode().trim());
		}
		if (entity.getCouponName() != null) {
			entity.setCouponName(entity.getCouponName().trim());
		}
		if (entity.getCouponType() != null) {
			CouponTypeEnums type = CouponTypeEnums.ofCode(entity.getCouponType());
			if (type != null) {
				entity.setCouponType(type.getCode());
			}
		}
		if (entity.getCurrency() != null) {
			CouponCurrencyEnums cur = CouponCurrencyEnums.ofCode(entity.getCurrency());
			if (cur != null) {
				entity.setCurrency(cur.getCode());
			}
		}
		if (entity.getScope() != null) {
			CouponScopeEnums scope = CouponScopeEnums.ofCode(entity.getScope());
			if (scope != null) {
				entity.setScope(scope.getCode());
			}
		}
	}

	private String validateTemplate(CouponTemplateEntity entity, boolean creating) {
		if (entity == null) {
			return I18nUtil.getMessage("base_error");
		}
		if (creating && StringUtils.isEmpty(entity.getCouponCode())) {
			return "couponCode 不能为空";
		}
		if (creating && StringUtils.isEmpty(entity.getCouponName())) {
			return "couponName 不能为空";
		}
		if (creating && !CouponTypeEnums.isValid(entity.getCouponType())) {
			return "couponType 须为 PERCENT / FULL_REDUCE / FIXED";
		}
		if (entity.getCouponType() != null && !CouponTypeEnums.isValid(entity.getCouponType())) {
			return "couponType 无效";
		}
		// 新增或本次改了类型时，校验面额字段
		if (entity.getCouponType() != null) {
			CouponTypeEnums type = CouponTypeEnums.ofCode(entity.getCouponType());
			if (type == CouponTypeEnums.PERCENT) {
				if (entity.getDiscountRate() == null
						|| entity.getDiscountRate().compareTo(BigDecimal.ZERO) <= 0
						|| entity.getDiscountRate().compareTo(BigDecimal.ONE) >= 0) {
					return "折扣率须在 (0,1) 之间，如 0.8 表示 8 折";
				}
			} else if (type == CouponTypeEnums.FULL_REDUCE) {
				if (entity.getThresholdAmt() == null
						|| entity.getThresholdAmt().compareTo(BigDecimal.ZERO) <= 0) {
					return "满减门槛须大于 0";
				}
				if (entity.getReduceAmt() == null
						|| entity.getReduceAmt().compareTo(BigDecimal.ZERO) <= 0) {
					return "减免额须大于 0";
				}
				if (entity.getReduceAmt().compareTo(entity.getThresholdAmt()) > 0) {
					return "减免额不能大于门槛";
				}
			} else if (type == CouponTypeEnums.FIXED) {
				if (entity.getReduceAmt() == null
						|| entity.getReduceAmt().compareTo(BigDecimal.ZERO) <= 0) {
					return "立减金额须大于 0";
				}
			}
		}
		if (creating) {
			if (!CouponCurrencyEnums.isValid(entity.getCurrency())) {
				return "currency 须为 YUAN 或 COIN";
			}
			if (StringUtils.isEmpty(entity.getScope())) {
				entity.setScope(CouponScopeEnums.ALL.getCode());
			} else if (!CouponScopeEnums.isValid(entity.getScope())) {
				return "scope 无效";
			}
		} else {
			if (entity.getCurrency() != null && !CouponCurrencyEnums.isValid(entity.getCurrency())) {
				return "currency 无效";
			}
			if (entity.getScope() != null && !CouponScopeEnums.isValid(entity.getScope())) {
				return "scope 无效";
			}
		}
		if (entity.getPerUserLimit() != null && entity.getPerUserLimit() < 1) {
			return "perUserLimit 须 >= 1";
		}
		if (entity.getTotalLimit() != null && entity.getTotalLimit() < 1) {
			return "totalLimit 须 >= 1";
		}
		if (entity.getValidDays() != null && entity.getValidDays() < 1) {
			return "validDays 须 >= 1";
		}
		if (entity.getStatus() != null
				&& !PublicEnums.ZERO.getIndex().equals(entity.getStatus())
				&& !PublicEnums.ONE.getIndex().equals(entity.getStatus())) {
			return "status 仅支持 0/1";
		}
		return null;
	}

	private String resolveOperatorName(Integer adminId) {
		if (adminId == null) {
			return "系统";
		}
		SysUserEntity admin = sysUserDao.findById(adminId);
		if (admin == null) {
			return "系统";
		}
		if (!StringUtils.isEmpty(admin.getUsername())) {
			return admin.getUsername();
		}
		return StringUtils.isEmpty(admin.getAcctive()) ? "系统" : admin.getAcctive();
	}

	private String buildTargetDesc(int requestCount, int issuedCount, List<String> skipReasons) {
		String base = "请求" + requestCount + "人，成功" + issuedCount + "人";
		if (skipReasons == null || skipReasons.isEmpty()) {
			return base;
		}
		String skip = skipReasons.stream().limit(5).collect(Collectors.joining("; "));
		return base + "；跳过:" + skip;
	}
}
