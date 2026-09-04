package com.playlet.internal.service.support;

import com.playlet.internal.api.response.WalletCardAdminResp;
import com.playlet.internal.dao.wallet.WalletCardLabelDao;
import com.playlet.internal.dao.wallet.WalletCardSynopsisDao;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.service.MediaUrlService;
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
	@Autowired
	private MediaUrlService mediaUrlService;

	public WalletCardAdminResp toAdminResp(WalletCardProductEntity row) {
		if (row == null) {
			return null;
		}
		WalletCardAdminResp resp = new WalletCardAdminResp();
		resp.setId(row.getId());
		resp.setUuid(row.getProductUuid());
		resp.setTitle(row.getCardTitle());
		resp.setBankCardNature(row.getBankcardNature());
		// 卡片类型 / 币种 / 月费 / 卡号段
		resp.setCardType(row.getBankcardNature());
		resp.setCurrency(row.getCurrency());
		resp.setMonthFee(row.getMonthFee());
		resp.setCardBin(row.getCardBin());
		resp.setCardBrand(row.getCardBrand());
		// 库内多为七牛 key，列表出参签名为可访问 URL
		resp.setImg(mediaUrlService.sign(row.getCardImg()));
		resp.setListImg(mediaUrlService.sign(row.getCardListImg()));
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
		if (row.getId() != null) {
			String cardRef = String.valueOf(row.getId());
			List<Integer> synopsisIds = walletCardSynopsisDao.querySynopsisIdsByCardId(cardRef);
			resp.setSynopsisIdList(synopsisIds);
		}
		return resp;
	}

	public void enrichLabelAndSynopsisIds(WalletCardProductEntity row) {
		if (row == null || row.getId() == null) {
			return;
		}
		String cardRef = String.valueOf(row.getId());
		List<Integer> labelIds = walletCardLabelDao.queryLabelIdsByCardId(cardRef);
		row.setLabelIdList(labelIds);
		row.setSynopsisIdList(walletCardSynopsisDao.querySynopsisIdsByCardId(cardRef));
	}
}
