package com.playlet.internal.service.impl;

import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.CoinConstants;
import com.playlet.internal.dao.coin.CoinGlobalConfigDao;
import com.playlet.internal.dao.coin.CoinRateDailyDao;
import com.playlet.internal.entity.coin.CoinGlobalConfigEntity;
import com.playlet.internal.entity.coin.CoinRateDailyEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.CoinConfigManageService;
import com.playlet.internal.service.support.CoinRateSupport;
import com.playlet.internal.service.support.IdleCoinClearService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端金币全局配置与日汇率。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CoinConfigManageServiceImpl implements CoinConfigManageService {

	@Autowired
	private CoinGlobalConfigDao coinGlobalConfigDao;
	@Autowired
	private CoinRateDailyDao coinRateDailyDao;
	@Autowired
	private CoinRateSupport coinRateSupport;
	@Autowired
	private IdleCoinClearService idleCoinClearService;

	@Override
	@SysLogAnnotation(module = "金币配置", type = "GET", remark = "查询全局配置")
	public ResponseBase get() {
		CoinGlobalConfigEntity cfg = coinGlobalConfigDao.findOne();
		Map<String, Object> data = new HashMap<>(4);
		data.put("config", cfg);
		data.put("coinPerYuan", coinRateSupport.resolveCoinPerYuan());
		data.put("latestRate", coinRateDailyDao.findLatest());
		return setResultSuccess(data, I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "金币配置", type = "POST", remark = "更新全局配置")
	public ResponseBase update(@RequestBody CoinGlobalConfigEntity entity) {
		try {
			if (entity == null || entity.getId() == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			CoinGlobalConfigEntity old = coinGlobalConfigDao.selectById(entity.getId());
			if (old == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			String err = validateConfig(entity);
			if (err != null) {
				return setResultError(err);
			}
			GenericityUtil.updateDate(entity);
			coinGlobalConfigDao.updateById(entity);
			log.info("coin global config update id={} rateMode={} idleClear={}",
					entity.getId(), entity.getRateMode(), entity.getIdleClearEnabled());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("coin config update biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("coin config update failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "金币配置", type = "GET", remark = "最近一日汇率")
	public ResponseBase latestRate() {
		return setResultSuccess(coinRateDailyDao.findLatest(), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "金币配置", type = "POST", remark = "录入日汇率")
	public ResponseBase saveDailyRate(@RequestBody CoinRateDailyEntity entity) {
		try {
			if (entity == null || entity.getBizDate() == null) {
				return setResultError("bizDate 不能为空");
			}
			boolean hasFormula = entity.getAdTotal() != null || entity.getEcpm() != null
					|| entity.getRRate() != null || entity.getCoinTotal() != null;
			CoinRateDailyEntity toSave;
			if (hasFormula) {
				long adTotal = entity.getAdTotal() == null ? 0L : entity.getAdTotal();
				long coinTotal = entity.getCoinTotal() == null ? 0L : entity.getCoinTotal();
				BigDecimal ecpm = entity.getEcpm() == null ? BigDecimal.ZERO : entity.getEcpm();
				BigDecimal rRate = entity.getRRate();
				if (rRate == null) {
					CoinGlobalConfigEntity cfg = coinGlobalConfigDao.findOne();
					rRate = cfg == null || cfg.getRRate() == null ? BigDecimal.ZERO : cfg.getRRate();
				}
				toSave = coinRateSupport.computeDailyRate(adTotal, ecpm, rRate, coinTotal);
				toSave.setBizDate(entity.getBizDate());
			} else {
				if (entity.getCoinPerYuan() == null || entity.getCoinPerYuan() < 0) {
					return setResultError("coinPerYuan 须 >= 0，或传广告公式字段由服务端计算");
				}
				toSave = new CoinRateDailyEntity();
				toSave.setBizDate(entity.getBizDate());
				toSave.setAdTotal(0L);
				toSave.setEcpm(BigDecimal.ZERO);
				toSave.setRRate(BigDecimal.ZERO);
				toSave.setCoinTotal(0L);
				toSave.setPCoin(BigDecimal.ZERO);
				toSave.setCoinPerYuan(entity.getCoinPerYuan());
			}
			String dateKey = new SimpleDateFormat("yyyy-MM-dd").format(toSave.getBizDate());
			CoinRateDailyEntity exist = coinRateDailyDao.findByBizDate(dateKey);
			if (exist == null) {
				GenericityUtil.setDate(toSave);
				coinRateDailyDao.insert(toSave);
				log.info("coin daily rate insert bizDate={} coinPerYuan={}", dateKey, toSave.getCoinPerYuan());
			} else {
				toSave.setId(exist.getId());
				GenericityUtil.updateDate(toSave);
				coinRateDailyDao.updateById(toSave);
				log.info("coin daily rate update bizDate={} coinPerYuan={}", dateKey, toSave.getCoinPerYuan());
			}
			return setResultSuccess(toSave, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("coin daily rate save biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("coin daily rate save failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	@Override
	@SysLogAnnotation(module = "金币配置", type = "POST", remark = "手动清币")
	public ResponseBase runIdleClear() {
		try {
			int n = idleCoinClearService.clearIdleUsers();
			Map<String, Object> data = new HashMap<>(2);
			data.put("cleared", n);
			log.info("manual idle coin clear cleared={}", n);
			return setResultSuccess(data, I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("manual idle coin clear biz error", e);
			throw e;
		} catch (Exception e) {
			log.error("manual idle coin clear failed", e);
			throw new BaseException("操作失败", e);
		}
	}

	private String validateConfig(CoinGlobalConfigEntity entity) {
		if (entity.getRateMode() != null
				&& entity.getRateMode() != CoinConstants.RATE_MODE_MANUAL
				&& entity.getRateMode() != CoinConstants.RATE_MODE_DAILY) {
			return "rateMode 仅支持 1手动/2按日";
		}
		if (entity.getManualCoinPerYuan() != null && entity.getManualCoinPerYuan() < 0) {
			return "manualCoinPerYuan 不能为负";
		}
		if (entity.getIdleClearDays() != null && entity.getIdleClearDays() < 1) {
			return "idleClearDays 须 >= 1";
		}
		if (entity.getIdleClearEnabled() != null
				&& entity.getIdleClearEnabled() != CoinConstants.IDLE_CLEAR_ON
				&& entity.getIdleClearEnabled() != CoinConstants.IDLE_CLEAR_OFF) {
			return "idleClearEnabled 仅支持 0/1";
		}
		if (entity.getRRate() != null && entity.getRRate().compareTo(BigDecimal.ZERO) < 0) {
			return "rRate 不能为负";
		}
		return null;
	}
}
