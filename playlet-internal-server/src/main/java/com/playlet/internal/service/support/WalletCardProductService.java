package com.playlet.internal.service.support;

import com.playlet.internal.api.response.ThirdBankcardProductResp;
import com.playlet.internal.api.response.WalletCardProductItemResp;
import com.playlet.internal.api.response.WalletCardProductSyncResp;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletCardProductDao;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * U 卡产品本地缓存：三方同步 + C 端列表查询。
 */
@Slf4j
@Service
public class WalletCardProductService {

	private static final int ENABLE_YES = 1;
	private static final int HOT_NO = 0;

	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private ThirdService thirdService;

	/** C 端可申请卡产品列表（仅 enable=1） */
	public List<WalletCardProductItemResp> listEnabledProducts() {
		List<WalletCardProductEntity> rows = walletCardProductDao.findEnabledList();
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<WalletCardProductItemResp> items = new ArrayList<>(rows.size());
		for (WalletCardProductEntity row : rows) {
			items.add(toItemResp(row));
		}
		return items;
	}

	/** 一键拉取三方卡产品并 upsert 到本地表 */
	@Transactional(rollbackFor = Exception.class)
	public WalletCardProductSyncResp syncFromThird() {
		List<ThirdBankcardProductResp> thirdList;
		try {
			thirdList = thirdService.listCardProducts();
		} catch (BaseException e) {
			log.error("wallet card product sync third failed", e);
			throw e;
		} catch (Exception e) {
			log.error("wallet card product sync third error", e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		if (thirdList == null || thirdList.isEmpty()) {
			log.warn("wallet card product sync third empty");
			return WalletCardProductSyncResp.of(0, 0, 0);
		}
		int inserted = 0;
		int updated = 0;
		Date now = new Date();
		for (ThirdBankcardProductResp third : thirdList) {
			if (third == null || third.getId() == null) {
				continue;
			}
			WalletCardProductEntity existed = walletCardProductDao.findById(third.getId());
			if (existed == null) {
				WalletCardProductEntity entity = buildFromThird(third, now);
				entity.setEnable(ENABLE_YES);
				entity.setHot(HOT_NO);
				entity.setSetTime(now);
				entity.setGmtModified(now);
				entity.setSyncTime(now);
				try {
					walletCardProductDao.insert(entity);
					inserted++;
				} catch (Exception e) {
					log.error("wallet card product sync insert failed productId={}", third.getId(), e);
					throw new BaseException(I18nUtil.getMessage("base_error"), e);
				}
			} else {
				applyThirdFields(existed, third, now);
				existed.setGmtModified(now);
				existed.setSyncTime(now);
				try {
					walletCardProductDao.updateById(existed);
					updated++;
				} catch (Exception e) {
					log.error("wallet card product sync update failed productId={}", third.getId(), e);
					throw new BaseException(I18nUtil.getMessage("base_error"), e);
				}
			}
		}
		log.info("wallet card product sync done total={} inserted={} updated={}",
				thirdList.size(), inserted, updated);
		return WalletCardProductSyncResp.of(thirdList.size(), inserted, updated);
	}

	/** 管理端维护本地展示字段 */
	@Transactional(rollbackFor = Exception.class)
	public void updateLocalFields(WalletCardProductEntity patch) {
		if (patch == null || patch.getId() == null) {
			throw new BaseException(I18nUtil.getMessage("parameter_error"));
		}
		WalletCardProductEntity existed = walletCardProductDao.findById(patch.getId());
		if (existed == null) {
			throw new BaseException(I18nUtil.getMessage("base_data_null"));
		}
		if (patch.getCardImg() != null) {
			existed.setCardImg(patch.getCardImg());
		}
		if (patch.getEnable() != null) {
			existed.setEnable(patch.getEnable());
		}
		if (patch.getHot() != null) {
			existed.setHot(patch.getHot());
		}
		if (patch.getDescription1() != null) {
			existed.setDescription1(patch.getDescription1());
		}
		if (patch.getDescription2() != null) {
			existed.setDescription2(patch.getDescription2());
		}
		if (patch.getCardTitle() != null) {
			existed.setCardTitle(patch.getCardTitle());
		}
		existed.setGmtModified(new Date());
		try {
			walletCardProductDao.updateById(existed);
		} catch (Exception e) {
			log.error("wallet card product update failed productId={}", patch.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet card product updated productId={} enable={} hot={}",
				patch.getId(), existed.getEnable(), existed.getHot());
	}

	public static WalletCardProductItemResp toItemResp(WalletCardProductEntity row) {
		WalletCardProductItemResp item = new WalletCardProductItemResp();
		item.setProductId(row.getId());
		item.setCardTitle(row.getCardTitle());
		item.setCardBin(row.getCardBin());
		item.setBankCardNature(row.getBankcardNature());
		item.setCardBrand(row.getCardBrand());
		item.setCardMode(row.getCardMode());
		item.setCurrency(StringUtils.isEmpty(row.getCurrency())
				? WalletConstants.DEFAULT_CURRENCY : row.getCurrency());
		if (row.getApplyFee() != null) {
			item.setApplyFee(row.getApplyFee().intValue());
		}
		if (row.getRechargeFee() != null) {
			item.setRechargeFee(row.getRechargeFee().doubleValue());
		}
		item.setBankcardRegion(row.getBankcardRegion());
		item.setActiveMinLimit(row.getActiveMinLimit());
		item.setRechargeMinLimit(row.getRechargeMinLimit());
		item.setCardImg(row.getCardImg());
		return item;
	}

	private static WalletCardProductEntity buildFromThird(ThirdBankcardProductResp third, Date now) {
		WalletCardProductEntity entity = new WalletCardProductEntity();
		entity.setId(third.getId());
		applyThirdFields(entity, third, now);
		return entity;
	}

	/** 同步三方字段；card_img/enable/hot/描述 由本地维护，更新时不覆盖已有值 */
	private static void applyThirdFields(WalletCardProductEntity target, ThirdBankcardProductResp third, Date now) {
		target.setCardTitle(third.getCardTitle());
		target.setBankcardNature(third.getBankCardNature());
		target.setBankcardType(third.getCardBrand());
		target.setCardBrand(third.getCardBrand());
		target.setCardBin(third.getCardBin());
		target.setCardMode(third.getCardMode());
		target.setBankcardRegion(third.getBankcardRegion());
		target.setActiveMinLimit(third.getActiveMinLimit());
		target.setRechargeMinLimit(third.getRechargeMinLimit());
		target.setCurrency(StringUtils.isEmpty(third.getCcy()) ? WalletConstants.DEFAULT_CURRENCY : third.getCcy());
		target.setApplyFee(toBigDecimal(third.getApplyFee()));
		target.setRechargeFee(toBigDecimal(third.getRechargeFee()));
		if (StringUtils.isEmpty(target.getCardImg()) && !StringUtils.isEmpty(third.getCardImg())) {
			target.setCardImg(third.getCardImg());
		}
		target.setSyncTime(now);
	}

	private static BigDecimal toBigDecimal(Number value) {
		if (value == null) {
			return null;
		}
		return BigDecimal.valueOf(value.doubleValue());
	}
}
