package com.playlet.internal.service.impl;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.pay.PayChannelConfigDao;
import com.playlet.internal.dao.pay.PayGlobalConfigDao;
import com.playlet.internal.entity.pay.PayChannelConfigEntity;
import com.playlet.internal.entity.pay.PayGlobalConfigEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.PayConfigManageService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端支付配置：支付渠道商户参数与全局结算/提现参数。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class PayConfigManageServiceImpl implements PayConfigManageService {

	/** 支持的支付渠道 */
	private static final Set<String> SUPPORT_CHANNELS = new HashSet<>(
			Arrays.asList("WECHAT", "ALIPAY", "DOUYIN"));

	/** 支持的结算周期 */
	private static final Set<String> SUPPORT_SETTLE_CYCLES = new HashSet<>(
			Arrays.asList("T+0", "T+1", "T+3", "T+7", "MONTH"));

	@Autowired
	private PayChannelConfigDao payChannelConfigDao;
	@Autowired
	private PayGlobalConfigDao payGlobalConfigDao;

	@Override
	@SysLogAnnotation(module = "支付配置", type = "GET", remark = "查询支付配置")
	public ResponseBase get() {
		List<PayChannelConfigEntity> channels = payChannelConfigDao.findList();
		PayGlobalConfigEntity global = payGlobalConfigDao.findOne();
		Map<String, Object> data = new HashMap<>(4);
		data.put("channels", channels);
		data.put("global", global);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "支付配置", type = "POST", remark = "更新支付渠道配置")
	public ResponseBase updateChannel(@RequestBody PayChannelConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			PayChannelConfigEntity old = payChannelConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateChannel(entity);
			if (err != null) {
				return setResultError(err);
			}
			old.setMerchantNo(entity.getMerchantNo());
			old.setAppId(entity.getAppId());
			old.setEnabled(entity.getEnabled() == null ? old.getEnabled() : entity.getEnabled());
			old.setRemark(entity.getRemark());
			GenericityUtil.updateDate(old);
			payChannelConfigDao.updateById(old);
			log.info("pay channel config update id={} channel={} enabled={}",
					old.getId(), old.getChannel(), old.getEnabled());
			return setResultSuccess(old, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("pay channel config update biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("pay channel config update failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "支付配置", type = "POST", remark = "更新支付全局配置")
	public ResponseBase updateGlobal(@RequestBody PayGlobalConfigEntity entity) {
		try {
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			String err = validateGlobal(entity);
			if (err != null) {
				return setResultError(err);
			}
			PayGlobalConfigEntity old = payGlobalConfigDao.findOne();
			if (old == null) {
				// 无默认行时兜底新建
				old = new PayGlobalConfigEntity();
				old.setSettleCycle(entity.getSettleCycle());
				old.setMinWithdrawAmount(entity.getMinWithdrawAmount());
				old.setRemark(entity.getRemark());
				GenericityUtil.setDate(old);
				payGlobalConfigDao.insert(old);
				log.info("pay global config insert settleCycle={} minWithdraw={}",
						old.getSettleCycle(), old.getMinWithdrawAmount());
				return setResultSuccess(old, I18nUtil.getMessage("base_success"));
			}
			old.setSettleCycle(entity.getSettleCycle());
			old.setMinWithdrawAmount(entity.getMinWithdrawAmount());
			if (entity.getRemark() != null) {
				old.setRemark(entity.getRemark());
			}
			GenericityUtil.updateDate(old);
			payGlobalConfigDao.updateById(old);
			log.info("pay global config update id={} settleCycle={} minWithdraw={}",
					old.getId(), old.getSettleCycle(), old.getMinWithdrawAmount());
			return setResultSuccess(old, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("pay global config update biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("pay global config update failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	private String validateChannel(PayChannelConfigEntity entity) {
		if (entity.getEnabled() != null
				&& entity.getEnabled() != 0 && entity.getEnabled() != 1) {
			return "enabled 仅支持 0/1";
		}
		if (entity.getChannel() != null && !SUPPORT_CHANNELS.contains(entity.getChannel())) {
			return "不支持的支付渠道：" + entity.getChannel();
		}
		// 启用前须至少配置商户号或 APPID
		if (entity.getEnabled() != null && entity.getEnabled() == 1) {
			String merchantNo = entity.getMerchantNo() != null ? entity.getMerchantNo() : oldOrNull(entity, "merchantNo");
			String appId = entity.getAppId() != null ? entity.getAppId() : oldOrNull(entity, "appId");
			if (StringUtils.isBlank(merchantNo) && StringUtils.isBlank(appId)) {
				return "启用前请先配置商户号或 APPID";
			}
		}
		return null;
	}

	private String validateGlobal(PayGlobalConfigEntity entity) {
		if (StringUtils.isBlank(entity.getSettleCycle())
				|| !SUPPORT_SETTLE_CYCLES.contains(entity.getSettleCycle())) {
			return "settleCycle 仅支持 T+0/T+1/T+3/T+7/MONTH";
		}
		if (entity.getMinWithdrawAmount() == null
				|| entity.getMinWithdrawAmount().compareTo(BigDecimal.ZERO) < 0) {
			return "minWithdrawAmount 不能为空且须 >= 0";
		}
		return null;
	}

	/** 渠道启用校验时读取库中旧值（避免前端只传部分字段导致误判） */
	private String oldOrNull(PayChannelConfigEntity entity, String field) {
		PayChannelConfigEntity old = payChannelConfigDao.selectById(entity.getId());
		if (old == null) {
			return null;
		}
		return "merchantNo".equals(field) ? old.getMerchantNo() : old.getAppId();
	}
}
