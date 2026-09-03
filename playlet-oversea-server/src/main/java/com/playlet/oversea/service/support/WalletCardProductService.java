package com.playlet.oversea.service.support;

import com.playlet.oversea.api.request.WalletCardProductListRequest;
import com.playlet.oversea.api.request.WalletCardProductUpdateRequest;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.api.response.ThirdBankcardProductResp;
import com.playlet.oversea.api.response.WalletCardProductItemResp;
import com.playlet.oversea.api.response.WalletCardProductLabelResp;
import com.playlet.oversea.api.response.WalletCardProductSynopsisResp;
import com.playlet.oversea.api.response.WalletCardProductSyncResp;
import com.playlet.oversea.constants.WalletConstants;
import com.playlet.oversea.dao.wallet.WalletCardLabelDao;
import com.playlet.oversea.dao.wallet.WalletCardProductDao;
import com.playlet.oversea.entity.wallet.WalletCardLabelEntity;
import com.playlet.oversea.entity.wallet.WalletCardProductEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.third.ThirdService;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
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
	private WalletCardLabelDao walletCardLabelDao;
	@Autowired
	private ThirdService thirdService;

	/** C 端可申请卡产品列表（仅 enable=1） */
	public List<WalletCardProductItemResp> listEnabledProducts() {
		return listEnabledProducts(null);
	}

	/**
	 * 卡产品列表（对齐 onetoken CardService.findList）：仅上架产品，可按卡性质筛选。
	 */
	public List<WalletCardProductItemResp> listEnabledProducts(String bankCardNature) {
		List<WalletCardProductEntity> rows = walletCardProductDao.findEnabledList(bankCardNature);
		if (rows == null || rows.isEmpty()) {
			return Collections.emptyList();
		}
		List<WalletCardProductItemResp> items = new ArrayList<>(rows.size());
		for (WalletCardProductEntity row : rows) {
			enrichLabelJoin(row, LanguageContext.getLanguage());
			items.add(toItemResp(row));
		}
		return items;
	}

	/** 对齐 onetoken findList 入参 */
	public List<WalletCardProductItemResp> findList(WalletCardProductListRequest query) {
		String bankCardNature = query == null ? null : query.getBankCardNature();
		return listEnabledProducts(bankCardNature);
	}

	/** 管理端：按 productId 查询卡产品详情（对齐 onetoken CardService.findById） */
	public WalletCardProductEntity findByProductId(Integer productId) {
		if (productId == null) {
			throw new BaseException(I18nUtil.getMessage("parameter_error"));
		}
		WalletCardProductEntity row = walletCardProductDao.findById(productId);
		if (row == null) {
			throw new BaseException(I18nUtil.getMessage("wallet.product_not_found"));
		}
		enrichAdminDisplay(Collections.singletonList(row));
		enrichLabelJoin(row, LanguageContext.getLanguage());
		return row;
	}

	/** C 端：按 productId 查询可申请卡产品详情（仅 enable=1） */
	public WalletCardProductItemResp findEnabledItemByProductId(Integer productId) {
		if (productId == null) {
			throw new BaseException(I18nUtil.getMessage("parameter_error"));
		}
		WalletCardProductEntity row = walletCardProductDao.findById(productId);
		if (row == null || !Integer.valueOf(ENABLE_YES).equals(row.getEnable())) {
			throw new BaseException(I18nUtil.getMessage("wallet.product_not_found"));
		}
		enrichLabelJoin(row, LanguageContext.getLanguage());
		return toItemResp(row);
	}

	/** 关联表标签优先覆盖 description2 解析结果 */
	private void enrichLabelJoin(WalletCardProductEntity row, String language) {
		if (row == null || StringUtils.isEmpty(row.getProductUuid())) {
			return;
		}
		String lang = StringUtils.isEmpty(language) ? LanguageContext.getLanguage() : language;
		List<WalletCardLabelEntity> joinLabels = walletCardLabelDao.findByCardId(row.getProductUuid(), lang);
		if (joinLabels == null || joinLabels.isEmpty()) {
			return;
		}
		List<WalletCardProductLabelResp> labelList = new ArrayList<>();
		List<Integer> labelIdList = new ArrayList<>();
		for (WalletCardLabelEntity label : joinLabels) {
			if (label == null) {
				continue;
			}
			labelIdList.add(label.getId());
			WalletCardProductLabelResp item = new WalletCardProductLabelResp();
			item.setId(label.getId());
			item.setName(label.getName());
			labelList.add(item);
		}
		row.setLabelList(labelList);
		row.setLabelIdList(labelIdList);
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

	/** 管理端维护本地展示字段（含标签列表、卡简介） */
	@Transactional(rollbackFor = Exception.class)
	public void updateLocalFields(WalletCardProductUpdateRequest patch) {
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
		if (patch.getCardTitle() != null) {
			existed.setCardTitle(patch.getCardTitle());
		}
		// 标签列表：null 不改；空数组清空；有值则用 | 拼接写入 description2
		if (patch.getLabelList() != null) {
			existed.setDescription2(joinLabelNames(patch.getLabelList()));
		}
		// 简介：null 不改；有对象则更新 content（及可选 title→卡名称）
		if (patch.getSynopsisData() != null) {
			WalletCardProductSynopsisResp synopsis = patch.getSynopsisData();
			if (synopsis.getContent() != null) {
				existed.setDescription1(synopsis.getContent());
			}
			if (synopsis.getTitle() != null && patch.getCardTitle() == null) {
				existed.setCardTitle(synopsis.getTitle());
			}
		}
		if (patch.getLogisticsMonery() != null) {
			existed.setLogisticsMonery(patch.getLogisticsMonery());
		}
		existed.setGmtModified(new Date());
		try {
			walletCardProductDao.updateById(existed);
		} catch (Exception e) {
			log.error("wallet card product update failed productId={}", patch.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("wallet card product updated productId={} enable={} hot={} labelSize={}",
				patch.getId(), existed.getEnable(), existed.getHot(),
				patch.getLabelList() == null ? null : patch.getLabelList().size());
	}

	/** 管理端列表补齐标签/简介展示字段 */
	public void enrichAdminDisplay(List<WalletCardProductEntity> rows) {
		if (rows == null || rows.isEmpty()) {
			return;
		}
		for (WalletCardProductEntity row : rows) {
			if (row == null) {
				continue;
			}
			row.setLabelList(buildLabelList(row.getDescription2()));
			row.setSynopsisData(buildSynopsis(row.getCardTitle(), row.getDescription1()));
		}
	}

	private static String joinLabelNames(List<WalletCardProductLabelResp> labelList) {
		if (labelList == null || labelList.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (WalletCardProductLabelResp label : labelList) {
			if (label == null || StringUtils.isEmpty(label.getName())) {
				continue;
			}
			String name = label.getName().trim();
			if (name.isEmpty()) {
				continue;
			}
			if (sb.length() > 0) {
				sb.append('|');
			}
			sb.append(name);
		}
		return sb.toString();
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
		BigDecimal openCardCost = resolveOpenCardCost(row);
		item.setOpenCardCost(toDouble(openCardCost));
		if (openCardCost != null) {
			item.setApplyFee(openCardCost.intValue());
		} else if (row.getApplyFee() != null) {
			item.setApplyFee(row.getApplyFee().intValue());
		}
		item.setPreSaveCost(toDouble(resolvePreSaveCost(row)));
		item.setMonthFee(toDouble(nz(row.getMonthFee())));
		item.setMaxBalance(toDouble(nz(row.getRechargeMaxLimit())));
		item.setLogisticsMonery(toDouble(resolveLogisticsMonery(row)));
		if (row.getRechargeFee() != null) {
			item.setRechargeFee(row.getRechargeFee().doubleValue());
		}
		item.setBankcardRegion(row.getBankcardRegion());
		item.setActiveMinLimit(row.getActiveMinLimit());
		item.setRechargeMinLimit(row.getRechargeMinLimit());
		item.setCardImg(row.getCardImg());
		// 对齐 worldpay findList：标签列表 + 简介对象（本地暂存于 description1/2）
		item.setLabelList(row.getLabelList() != null && !row.getLabelList().isEmpty()
				? row.getLabelList() : buildLabelList(row.getDescription2()));
		item.setSynopsisData(buildSynopsis(row.getCardTitle(), row.getDescription1()));
		return item;
	}

	/** 实体卡邮费：仅 PHYSICAL 产品有值 */
	public static BigDecimal resolveLogisticsMonery(WalletCardProductEntity row) {
		if (row == null || !WalletConstants.BANKCARD_NATURE_PHYSICAL.equalsIgnoreCase(row.getBankcardNature())) {
			return BigDecimal.ZERO;
		}
		return nz(row.getLogisticsMonery());
	}

	/** 开卡费：open_card_cost 优先，否则 apply_fee */
	public static BigDecimal resolveOpenCardCost(WalletCardProductEntity row) {
		if (row == null) {
			return BigDecimal.ZERO;
		}
		if (row.getOpenCardCost() != null) {
			return row.getOpenCardCost();
		}
		return nz(row.getApplyFee());
	}

	/** 预存费：实体卡为 0；有配置用 pre_save_cost，否则虚拟卡回退 activeMinLimit */
	public static BigDecimal resolvePreSaveCost(WalletCardProductEntity row) {
		if (row == null) {
			return BigDecimal.ZERO;
		}
		if (WalletConstants.BANKCARD_NATURE_PHYSICAL.equalsIgnoreCase(row.getBankcardNature())) {
			return BigDecimal.ZERO;
		}
		if (row.getPreSaveCost() != null) {
			return row.getPreSaveCost();
		}
		if (row.getActiveMinLimit() != null && row.getActiveMinLimit() > 0) {
			return BigDecimal.valueOf(row.getActiveMinLimit());
		}
		return BigDecimal.ZERO;
	}

	private static BigDecimal nz(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private static Double toDouble(BigDecimal value) {
		if (value == null) {
			return 0.0D;
		}
		return value.doubleValue();
	}

	/** description2 → 卡标签；支持 | / ， / , 分隔多标签 */
	private static List<WalletCardProductLabelResp> buildLabelList(String description2) {
		if (StringUtils.isEmpty(description2)) {
			return Collections.emptyList();
		}
		String[] parts = description2.split("[|，,]");
		List<WalletCardProductLabelResp> list = new ArrayList<>();
		for (String part : parts) {
			if (part == null) {
				continue;
			}
			String name = part.trim();
			if (name.isEmpty()) {
				continue;
			}
			WalletCardProductLabelResp label = new WalletCardProductLabelResp();
			label.setName(name);
			list.add(label);
		}
		return list;
	}

	/** description1 → 卡简介 content；title 优先用卡名称 */
	private static WalletCardProductSynopsisResp buildSynopsis(String cardTitle, String description1) {
		if (StringUtils.isEmpty(description1) && StringUtils.isEmpty(cardTitle)) {
			return null;
		}
		WalletCardProductSynopsisResp synopsis = new WalletCardProductSynopsisResp();
		synopsis.setTitle(cardTitle);
		synopsis.setContent(description1);
		return synopsis;
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
		if (third.getRechargeMaxLimit() != null) {
			target.setRechargeMaxLimit(toBigDecimal(third.getRechargeMaxLimit()));
		}
		target.setCurrency(StringUtils.isEmpty(third.getCcy()) ? WalletConstants.DEFAULT_CURRENCY : third.getCcy());
		target.setApplyFee(toBigDecimal(third.getApplyFee()));
		if (third.getOpenCardCost() != null) {
			target.setOpenCardCost(toBigDecimal(third.getOpenCardCost()));
		}
		if (third.getPreSaveCost() != null) {
			target.setPreSaveCost(toBigDecimal(third.getPreSaveCost()));
		}
		if (third.getMonthFee() != null) {
			target.setMonthFee(toBigDecimal(third.getMonthFee()));
		}
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
