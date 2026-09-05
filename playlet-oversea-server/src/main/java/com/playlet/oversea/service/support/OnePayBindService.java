package com.playlet.oversea.service.support;

import com.playlet.oversea.api.request.OnePayBindVerifyRequest;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.enums.OnePayBindStatusEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.RedisUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 作家端 OnePay 绑定、解绑流程。
 */
@Slf4j
@Service
public class OnePayBindService extends BaseApiService {

	@Autowired
	private OnePayVerifyClient onePayVerifyClient;
	@Autowired
	private RedisUtil redisUtil;

	public ResponseBase bind(Integer uid, OnePayBindVerifyRequest query, String loginEmail,
			String emailCodeKeyPrefix, Integer currentBindStatus, OnePayBindOps ops) {
		ResponseBase paramErr = validateBindRequest(query, loginEmail, emailCodeKeyPrefix);
		if (paramErr != null) {
			return paramErr;
		}
		if (Integer.valueOf(OnePayBindStatusEnums.BOUND.getCode()).equals(currentBindStatus)) {
			return setResultError(I18nUtil.getMessage("withdraw.onepay_already_bound"));
		}
		String onepayAccount = query.getAccount().trim();
		try {
			String openid = onePayVerifyClient.verifyAccount(query, uid, onepayAccount);
			if (StringUtils.isEmpty(openid)) {
				return setResultError(I18nUtil.getMessage("withdraw.onepay_verify_failed"));
			}
			ops.bind(uid, onepayAccount, openid, new Date());
			redisUtil.del(emailCodeKeyPrefix + loginEmail);
			log.info("bind onepay uid={} account={}", uid, OnePayVerifyClient.maskAccount(onepayAccount));
		} catch (Exception e) {
			log.error("bind onepay failed uid={} account={}", uid, OnePayVerifyClient.maskAccount(onepayAccount), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	public ResponseBase unbind(Integer uid, OnePayBindVerifyRequest query, String loginEmail,
			String emailCodeKeyPrefix, Integer currentBindStatus, String currentOnepayAccount, OnePayBindOps ops) {
		ResponseBase paramErr = validateBindRequest(query, loginEmail, emailCodeKeyPrefix);
		if (paramErr != null) {
			return paramErr;
		}
		if (!Integer.valueOf(OnePayBindStatusEnums.BOUND.getCode()).equals(currentBindStatus)
				|| StringUtils.isEmpty(currentOnepayAccount)) {
			return setResultError(I18nUtil.getMessage("withdraw.onepay_not_bound"));
		}
		if (!currentOnepayAccount.equalsIgnoreCase(query.getAccount().trim())) {
			return setResultError(I18nUtil.getMessage("withdraw.onepay_account_mismatch"));
		}
		if (ops.countProcessingWithdraw(uid) > 0) {
			return setResultError(I18nUtil.getMessage("withdraw.onepay_pending"));
		}
		try {
			ops.unbind(uid);
			redisUtil.del(emailCodeKeyPrefix + loginEmail);
			log.info("unbind onepay uid={} account={}", uid, OnePayVerifyClient.maskAccount(query.getAccount()));
		} catch (Exception e) {
			log.error("unbind onepay failed uid={}", uid, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private ResponseBase validateBindRequest(OnePayBindVerifyRequest query, String loginEmail,
			String emailCodeKeyPrefix) {
		if (query == null || StringUtils.isEmpty(query.getAccount())
				|| StringUtils.isEmpty(query.getVerificationCode())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (StringUtils.isEmpty(loginEmail) || !verifyCode(emailCodeKeyPrefix, loginEmail, query.getVerificationCode())) {
			return setResultError(I18nUtil.getMessage("incorrect_or_expired__verification_code"));
		}
		return null;
	}

	private boolean verifyCode(String emailCodeKeyPrefix, String email, String code) {
		if (StringUtils.isEmpty(code)) {
			return false;
		}
		Object cache = redisUtil.get(emailCodeKeyPrefix + email);
		return cache != null && cache.toString().equals(code);
	}
}
