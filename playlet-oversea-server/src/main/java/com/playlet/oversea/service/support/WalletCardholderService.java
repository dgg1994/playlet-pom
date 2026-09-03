package com.playlet.oversea.service.support;

import com.playlet.oversea.api.request.WalletCardHolderRequest;
import com.playlet.oversea.api.request.WalletCardholderSaveRequest;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.dao.wallet.WalletUserHolderDao;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.entity.wallet.WalletUserHolderEntity;
import com.playlet.oversea.enums.WalletSexEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.utils.AgeCheckUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.KycFieldNormalizeUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 持卡人资料维护（对齐 worldpay CardholderService）。
 */
@Slf4j
@Service
public class WalletCardholderService extends BaseApiService {

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletUserHolderDao walletUserHolderDao;

	/** 新增持卡人 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase add(Integer userType, Integer localUid, WalletCardholderSaveRequest query) {
		WalletUserEntity user = requireWalletUser(userType, localUid);
		validateSaveRequest(query, false);
		if (!AgeCheckUtil.isAdult(query.getUserBirthday())) {
			return setResultError(I18nUtil.getMessage("holder_age_error"));
		}
		Date now = new Date();
		WalletUserHolderEntity holder = toHolderEntity(user, query, now);
		try {
			walletUserHolderDao.insert(holder);
		} catch (Exception e) {
			log.error("wallet cardholder add failed walletUserId={}", user.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet cardholder add success walletUserId={} holderId={}", user.getId(), holder.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 编辑持卡人 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase update(Integer userType, Integer localUid, WalletCardholderSaveRequest query) {
		if (query == null || query.getId() == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletUserEntity user = requireWalletUser(userType, localUid);
		WalletUserHolderEntity existed = walletUserHolderDao.findOwned(query.getId(), user.getId());
		if (existed == null) {
			return setResultError(I18nUtil.getMessage("holder_null"));
		}
		validateSaveRequest(query, true);
		if (!StringUtils.isEmpty(query.getUserBirthday()) && !AgeCheckUtil.isAdult(query.getUserBirthday())) {
			return setResultError(I18nUtil.getMessage("holder_age_error"));
		}
		fillHolderFromRequest(existed, query);
		existed.setGmtModified(new Date());
		try {
			walletUserHolderDao.updateById(existed);
		} catch (Exception e) {
			log.error("wallet cardholder update failed holderId={}", query.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet cardholder update success walletUserId={} holderId={}", user.getId(), query.getId());
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 删除持卡人 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase delete(Integer userType, Integer localUid, Long id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletUserEntity user = requireWalletUser(userType, localUid);
		WalletUserHolderEntity existed = walletUserHolderDao.findOwned(id, user.getId());
		if (existed == null) {
			return setResultError(I18nUtil.getMessage("holder_null"));
		}
		try {
			walletUserHolderDao.deleteById(id);
		} catch (Exception e) {
			log.error("wallet cardholder delete failed holderId={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet cardholder delete success walletUserId={} holderId={}", user.getId(), id);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	/** 当前用户持卡人列表 */
	public ResponseBase list(Integer userType, Integer localUid) {
		WalletUserEntity user = requireWalletUser(userType, localUid);
		List<WalletUserHolderEntity> list = walletUserHolderDao.findByWalletUserId(user.getId());
		return setResultSuccess(list, I18nUtil.getMessage("base_success"));
	}

	/** 按 id 查询持卡人 */
	public ResponseBase findById(Integer userType, Integer localUid, Long id) {
		if (id == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletUserEntity user = requireWalletUser(userType, localUid);
		WalletUserHolderEntity holder = walletUserHolderDao.findOwned(id, user.getId());
		if (holder == null) {
			return setResultError(I18nUtil.getMessage("holder_null"));
		}
		return setResultSuccess(holder, I18nUtil.getMessage("base_success"));
	}

	/**
	 * 开卡申请时新建持卡人（复用校验与落库逻辑）。
	 */
	@Transactional(rollbackFor = Exception.class)
	public WalletUserHolderEntity createForApply(WalletUserEntity user, WalletCardHolderRequest data) {
		WalletCardholderSaveRequest save = toSaveRequest(data);
		validateSaveRequest(save, false);
		if (!AgeCheckUtil.isAdult(save.getUserBirthday())) {
			throw new BaseException(I18nUtil.getMessage("holder_age_error"));
		}
		Date now = new Date();
		WalletUserHolderEntity holder = toHolderEntity(user, save, now);
		try {
			walletUserHolderDao.insert(holder);
		} catch (Exception e) {
			log.error("wallet cardholder createForApply failed walletUserId={}", user.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return holder;
	}

	private WalletUserEntity requireWalletUser(Integer userType, Integer localUid) {
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.not_opened"));
		}
		return user;
	}

	private static WalletCardholderSaveRequest toSaveRequest(WalletCardHolderRequest data) {
		WalletCardholderSaveRequest save = new WalletCardholderSaveRequest();
		save.setUserName(data.getUserName());
		save.setUserSurname(data.getUserSurname());
		save.setUserTelDialCode(data.getUserTelDialCode());
		save.setUserTelCode(data.getUserTelCode());
		save.setUserTel(data.getUserTel());
		save.setUserEmail(data.getUserEmail());
		save.setUserNumber(data.getUserNumber());
		save.setUserSex(data.getUserSex());
		save.setUserSexNum(data.getUserSexNum());
		save.setUserAddress(data.getUserAddress());
		save.setUserBirthday(data.getUserBirthday());
		return save;
	}

	private static void validateSaveRequest(WalletCardholderSaveRequest query, boolean update) {
		if (query == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.apply_holder_required"));
		}
		if (StringUtils.isEmpty(query.getUserName()) || StringUtils.isEmpty(query.getUserSurname())
				|| StringUtils.isEmpty(query.getUserTel()) || StringUtils.isEmpty(query.getUserEmail())) {
			throw new BaseException(I18nUtil.getMessage("wallet.apply_holder_required"));
		}
		if (!update && StringUtils.isEmpty(query.getUserBirthday())) {
			throw new BaseException(I18nUtil.getMessage("holder_birth_error"));
		}
	}

	private static WalletUserHolderEntity toHolderEntity(WalletUserEntity user, WalletCardholderSaveRequest query,
			Date now) {
		WalletUserHolderEntity holder = new WalletUserHolderEntity();
		holder.setWalletUserId(user.getId());
		holder.setWalletUid(user.getWalletUid());
		fillHolderFromRequest(holder, query);
		holder.setSetTime(now);
		holder.setGmtModified(now);
		return holder;
	}

	private static void fillHolderFromRequest(WalletUserHolderEntity holder, WalletCardholderSaveRequest query) {
		if (!StringUtils.isEmpty(query.getUserName())) {
			holder.setUserName(query.getUserName().trim());
		}
		if (!StringUtils.isEmpty(query.getUserSurname())) {
			holder.setUserSurname(query.getUserSurname().trim());
		}
		KycFieldNormalizeUtil.normalizeHolderTel(query.getUserTelDialCode(), query.getUserTelCode(),
				query.getUserTel(), (dialCode, telCode, tel) -> {
					holder.setUserTelDialCode(dialCode);
					holder.setUserTelCode(telCode);
					holder.setUserTel(tel);
				});
		if (!StringUtils.isEmpty(query.getUserEmail())) {
			holder.setUserEmail(query.getUserEmail().trim());
		}
		holder.setUserNumber(query.getUserNumber());
		holder.setUserSexNum(query.getUserSexNum());
		// 性别编号优先推导文案
		if (query.getUserSexNum() != null) {
			String sexLabel = WalletSexEnums.labelOf(query.getUserSexNum());
			if (!StringUtils.isEmpty(sexLabel)) {
				holder.setUserSex(sexLabel);
			}
		} else if (!StringUtils.isEmpty(query.getUserSex())) {
			holder.setUserSex(query.getUserSex());
		}
		holder.setUserAddress(query.getUserAddress());
		if (!StringUtils.isEmpty(query.getUserBirthday())) {
			holder.setUserBirthday(query.getUserBirthday().trim());
		}
	}
}
