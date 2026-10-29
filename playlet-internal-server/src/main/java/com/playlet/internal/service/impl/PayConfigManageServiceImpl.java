package com.playlet.internal.service.impl;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.PayConfigLoader;
import com.playlet.internal.dao.pay.PayGlobalConfigDao;
import com.playlet.internal.dao.pay.PaySdkConfigDao;
import com.playlet.internal.entity.pay.PayGlobalConfigEntity;
import com.playlet.internal.entity.pay.PaySdkConfigEntity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

	/** 支持的结算周期 */
	private static final Set<String> SUPPORT_SETTLE_CYCLES = new HashSet<>(
			Arrays.asList("T+0", "T+1", "T+3", "T+7", "MONTH"));

	@Autowired
	private PayGlobalConfigDao payGlobalConfigDao;
	@Autowired
	private PaySdkConfigDao paySdkConfigDao;
	@Autowired
	private PayConfigLoader payConfigLoader;

	@Override
	@SysLogAnnotation(module = "支付配置", type = "GET", remark = "查询支付配置")
	public ResponseBase get() {
		PayGlobalConfigEntity global = payGlobalConfigDao.findOne();
		Map<String, Object> data = new HashMap<>(4);
		data.put("global", global);
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
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

	@Override
	@SysLogAnnotation(module = "支付配置", type = "GET", remark = "查询支付SDK商户参数")
	public ResponseBase sdkList() {
		Map<String, PaySdkConfigEntity> byKey = new HashMap<>(16);
		for (PaySdkConfigEntity row : paySdkConfigDao.findList()) {
			byKey.put(row.getConfigKey(), row);
		}
		List<Map<String, Object>> items = new ArrayList<>();
		for (Map.Entry<String, String> entry : PayConfigLoader.KNOWN_KEYS.entrySet()) {
			String key = entry.getKey();
			boolean secret = payConfigLoader.isSecretKey(key);
			PaySdkConfigEntity row = byKey.get(key);
			boolean configured = row != null && StringUtils.isNotBlank(row.getConfigValue());
			Map<String, Object> item = new HashMap<>(8);
			item.put("configKey", key);
			item.put("remark", entry.getValue());
			item.put("secret", secret);
			item.put("configured", configured);
			// 敏感项永不回显明文/密文，前端据 configured 展示占位
			item.put("value", !secret && configured ? row.getConfigValue() : null);
			item.put("encrypted", row != null && Integer.valueOf(1).equals(row.getEncrypted()));
			item.put("gmtModified", row == null ? null : row.getGmtModified());
			items.add(item);
		}
		Map<String, Object> data = new HashMap<>(4);
		data.put("items", items);
		data.put("encryptEnabled", payConfigLoader.encryptEnabled());
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "支付配置", type = "POST", remark = "更新支付SDK商户参数")
	public ResponseBase sdkUpdate(@RequestBody List<PaySdkConfigEntity> items) {
		try {
			if (items == null || items.isEmpty()) {
				return setResultError(I18nUtil.getMessage("param_not_null"));
			}
			Date now = new Date();
			int updated = 0;
			for (PaySdkConfigEntity item : items) {
				String key = item == null ? null : item.getConfigKey();
				if (StringUtils.isEmpty(key) || !PayConfigLoader.KNOWN_KEYS.containsKey(key)) {
					return setResultError("不支持的支付配置键：" + key);
				}
				// null 表示未修改跳过；空串表示清空该键（DB 不再生效，回落 yml 默认）
				String value = item.getConfigValue() == null ? null : item.getConfigValue().trim();
				if (value == null) {
					continue;
				}
				boolean secret = payConfigLoader.isSecretKey(key);
				boolean clear = value.isEmpty();
				if (secret && !clear) {
					if (!payConfigLoader.encryptEnabled()) {
						return setResultError("未配置 pay.config.encrypt-key，无法加密存储敏感项：" + key);
					}
					value = payConfigLoader.encrypt(value);
				}
				PaySdkConfigEntity row = paySdkConfigDao.findByKey(key);
				if (row == null) {
					row = new PaySdkConfigEntity();
					row.setConfigKey(key);
					row.setSetTime(now);
				}
				row.setConfigValue(clear ? null : value);
				row.setEncrypted(secret && !clear ? 1 : 0);
				row.setGmtModified(now);
				if (row.getId() == null) {
					paySdkConfigDao.insert(row);
				} else {
					paySdkConfigDao.updateById(row);
				}
				updated++;
			}
			int applied = payConfigLoader.applyFromDb();
			payConfigLoader.invalidateClients();
			log.info("pay sdk config updated keys={} applied={}", updated, applied);
			Map<String, Object> data = new HashMap<>(4);
			data.put("updated", updated);
			data.put("applied", applied);
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (IllegalStateException e) {
			log.error("pay sdk config update encrypt error", e);
			return setResultError(e.getMessage());
		} catch (BaseException e) {
			log.error("pay sdk config update biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("pay sdk config update failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "支付配置", type = "POST", remark = "刷新支付配置并重建支付客户端")
	public ResponseBase sdkRefresh() {
		try {
			int applied = payConfigLoader.applyFromDb();
			payConfigLoader.invalidateClients();
			Map<String, Object> data = new HashMap<>(4);
			data.put("applied", applied);
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (IllegalStateException e) {
			log.error("pay sdk config refresh decrypt error", e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("pay sdk config refresh failed", e);
			throw new BaseException("操作失败", e);
		}
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
}
