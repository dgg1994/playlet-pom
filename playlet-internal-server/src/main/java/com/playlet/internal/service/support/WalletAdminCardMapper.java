package com.playlet.internal.service.support;

import com.playlet.internal.api.response.WalletCardAdminResp;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.wallet.WalletCardLabelDao;
import com.playlet.internal.dao.wallet.WalletCardSynopsisDao;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 管理端卡产品展示字段转换（onetoken 字段名 ↔ 本地实体）。
 */
@Component
public class WalletAdminCardMapper {

	@Autowired
	private WalletCardLabelDao walletCardLabelDao;
	@Autowired
	private WalletCardSynopsisDao walletCardSynopsisDao;

	public WalletCardAdminResp toAdminResp(WalletCardProductEntity row) {
		if (row == null) {
			return null;
		}
		WalletCardAdminResp resp = new WalletCardAdminResp();
		resp.setId(row.getId());
		resp.setUuid(row.getProductUuid());
		resp.setTitle(row.getCardTitle());
		resp.setBankCardNature(row.getBankcardNature());
		resp.setImg(row.getCardImg());
		resp.setListImg(row.getCardListImg());
		resp.setOpenCardCost(row.getOpenCardCost());
		resp.setPreSaveCost(row.getPreSaveCost());
		resp.setRechargeFee(row.getRechargeFee());
		resp.setActiveMinLimit(row.getActiveMinLimit());
		resp.setRechargeMinLimit(row.getRechargeMinLimit());
		resp.setRechargeMaxLimit(row.getRechargeMaxLimit());
		resp.setEnable(row.getEnable());
		resp.setHot(row.getHot());
		resp.setLableList(row.getLabelList());
		resp.setLableIdList(row.getLabelIdList());
		resp.setSynopsisData(row.getSynopsisData());
		if (row.getProductUuid() != null) {
			List<Integer> synopsisIds = walletCardSynopsisDao.querySynopsisIdsByCardId(row.getProductUuid());
			resp.setSynopsisIdList(synopsisIds);
		}
		return resp;
	}

	public void enrichLabelAndSynopsisIds(WalletCardProductEntity row) {
		if (row == null || row.getProductUuid() == null) {
			return;
		}
		String lang = LanguageContext.getLanguage();
		List<Integer> labelIds = walletCardLabelDao.queryLabelIdsByCardId(row.getProductUuid());
		row.setLabelIdList(labelIds);
		row.setSynopsisIdList(walletCardSynopsisDao.querySynopsisIdsByCardId(row.getProductUuid()));
	}
}
