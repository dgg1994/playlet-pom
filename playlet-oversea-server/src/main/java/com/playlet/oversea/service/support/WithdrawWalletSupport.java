package com.playlet.oversea.service.support;

import com.playlet.oversea.dao.wallet.WalletAccountDao;
import com.playlet.oversea.dao.wallet.WalletBankcardDao;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.entity.wallet.WalletAccountEntity;
import com.playlet.oversea.entity.wallet.WalletBankcardEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.enums.WalletCardStatusEnums;
import com.playlet.oversea.enums.WalletKycStateEnums;
import com.playlet.oversea.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 提现页钱包就绪状态：KYC 通过 + 可用默认 U 卡。
 */
@Component
public class WithdrawWalletSupport {

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletBankcardDao walletBankcardDao;

	/** 写入快照：是否可提现到 U 卡及目标卡信息 */
	public void enrich(Integer userType, Integer localUid, WithdrawWalletSnapshot snap) {
		if (userType == null || localUid == null || snap == null) {
			return;
		}
		snap.setWalletWithdrawReady(0);
		WalletUserEntity user = walletUserDao.findByLocal(userType, localUid);
		if (user == null) {
			return;
		}
		snap.setWalletUid(user.getWalletUid());
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null
				|| !Integer.valueOf(WalletKycStateEnums.SUCCESS_APPROVE.getCode()).equals(account.getKycState())) {
			return;
		}
		WalletBankcardEntity card = resolvePayoutCard(user.getId());
		if (card == null) {
			return;
		}
		snap.setWalletWithdrawReady(1);
		snap.setTargetBankcardId(card.getId());
		snap.setTargetUserBankcardId(card.getUserBankcardId());
		snap.setPayoutTargetMasked(maskCardNo(card.getCardNo()));
	}

	/** 按本地卡 id 取脱敏卡号 */
	public String maskTargetByBankcardId(Long bankcardId) {
		if (bankcardId == null) {
			return null;
		}
		WalletBankcardEntity card = walletBankcardDao.selectById(bankcardId);
		if (card == null) {
			return null;
		}
		return maskCardNo(card.getCardNo());
	}

	private WalletBankcardEntity resolvePayoutCard(Long walletUserId) {
		WalletBankcardEntity card = walletBankcardDao.findDefaultByWalletUserId(walletUserId);
		if (isActiveCard(card)) {
			return card;
		}
		List<WalletBankcardEntity> cards = walletBankcardDao.findByWalletUserId(walletUserId);
		if (cards == null || cards.isEmpty()) {
			return null;
		}
		for (WalletBankcardEntity item : cards) {
			if (isActiveCard(item)) {
				return item;
			}
		}
		return null;
	}

	private static boolean isActiveCard(WalletBankcardEntity card) {
		return card != null
				&& card.getUserBankcardId() != null
				&& Integer.valueOf(WalletCardStatusEnums.ACTIVE.getCode()).equals(card.getCardStatus());
	}

	/** 卡号脱敏：保留后四位 */
	public static String maskCardNo(String cardNo) {
		if (StringUtils.isEmpty(cardNo)) {
			return cardNo;
		}
		String trimmed = cardNo.trim();
		if (trimmed.length() <= 4) {
			return "****";
		}
		return "****" + trimmed.substring(trimmed.length() - 4);
	}
}
