package com.playlet.internal.service.support;

import com.playlet.internal.api.response.ThirdBankcardInfoResp;
import com.playlet.internal.api.response.ThirdUserBankcardResp;
import com.playlet.internal.dao.wallet.WalletBankcardDao;
import com.playlet.internal.entity.wallet.WalletBankcardEntity;
import com.playlet.internal.service.third.ThirdService;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从三方补全本地 wallet_bankcard.card_no（Webhook 未带卡号时的兜底）。
 */
@Slf4j
@Service
public class WalletBankcardSyncSupport {

	@Autowired
	private ThirdService thirdService;
	@Autowired
	private WalletBankcardDao walletBankcardDao;

	/** 列表展示前：批量拉三方用户卡列表补全缺失卡号 */
	public void syncMissingCardNos(Long walletUid, List<WalletBankcardEntity> cards) {
		if (walletUid == null || cards == null || cards.isEmpty()) {
			return;
		}
		boolean needSync = false;
		for (WalletBankcardEntity card : cards) {
			if (StringUtils.isEmpty(card.getCardNo())) {
				needSync = true;
				break;
			}
		}
		if (!needSync) {
			return;
		}
		Map<Long, String> thirdMap = loadThirdCardNumberMap(walletUid);
		for (WalletBankcardEntity card : cards) {
			if (!StringUtils.isEmpty(card.getCardNo())) {
				continue;
			}
			persistCardNo(card, thirdMap.get(card.getUserBankcardId()));
		}
	}

	/** 单卡补全卡号：用户卡列表 → /bankcard/info */
	public String syncCardNo(WalletBankcardEntity card) {
		if (card == null || card.getWalletUid() == null || card.getUserBankcardId() == null) {
			return null;
		}
		if (!StringUtils.isEmpty(card.getCardNo())) {
			return card.getCardNo();
		}
		String cardNo = loadThirdCardNumberMap(card.getWalletUid()).get(card.getUserBankcardId());
		if (StringUtils.isEmpty(cardNo)) {
			cardNo = resolveCardNoFromInfo(card.getWalletUid(), card.getUserBankcardId());
		}
		persistCardNo(card, cardNo);
		return card.getCardNo();
	}

	private void persistCardNo(WalletBankcardEntity card, String cardNo) {
		if (card == null || StringUtils.isEmpty(cardNo)) {
			return;
		}
		try {
			walletBankcardDao.updateCardNo(card.getId(), cardNo.trim());
			card.setCardNo(cardNo.trim());
			log.info("wallet sync cardNo success userBankcardId={}", card.getUserBankcardId());
		} catch (Exception e) {
			log.error("wallet sync cardNo persist failed userBankcardId={}", card.getUserBankcardId(), e);
		}
	}

	private String resolveCardNoFromInfo(Long walletUid, Long userBankcardId) {
		try {
			ThirdBankcardInfoResp info = thirdService.getBankcardInfo(walletUid, userBankcardId);
			return info == null ? null : info.getCardNumber();
		} catch (Exception e) {
			log.error("wallet sync cardNo info failed walletUid={} userBankcardId={}",
					walletUid, userBankcardId, e);
			return null;
		}
	}

	private Map<Long, String> loadThirdCardNumberMap(Long walletUid) {
		try {
			List<ThirdUserBankcardResp> list = thirdService.listUserCards(walletUid);
			if (list == null || list.isEmpty()) {
				return Collections.emptyMap();
			}
			Map<Long, String> map = new HashMap<>(list.size());
			for (ThirdUserBankcardResp item : list) {
				if (item.getUserBankcardId() == null || StringUtils.isEmpty(item.getCardNumber())) {
					continue;
				}
				map.put(item.getUserBankcardId(), item.getCardNumber().trim());
			}
			return map;
		} catch (Exception e) {
			log.error("wallet sync cardNo list failed walletUid={}", walletUid, e);
			return Collections.emptyMap();
		}
	}
}
