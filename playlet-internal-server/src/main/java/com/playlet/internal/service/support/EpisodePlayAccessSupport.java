package com.playlet.internal.service.support;

import com.playlet.internal.constants.EpisodeUnlockConstants;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.member.UserEpisodeUnlockDao;
import com.playlet.internal.dao.member.UserMemberDao;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.member.UserEpisodeUnlockEntity;
import com.playlet.internal.enums.EpisodeUnlockTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * C 端单集播放策略：会员短剧 / 免费 / 广告 / 付费。
 * <p>
 * VIP：会员短剧免费 + 广告集免广告；普通付费集仍需解锁。
 */
@Service
public class EpisodePlayAccessSupport {

	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private UserMemberDao userMemberDao;
	@Autowired
	private UserEpisodeUnlockDao userEpisodeUnlockDao;

	/**
	 * 按已上架集判定播放权限。
	 */
	public EpisodePlayAccessResult resolve(Integer uid, Integer assetId) {
		EpisodePlayAccessResult result = new EpisodePlayAccessResult();
		result.setAssetId(assetId);
		result.setCanPlay(false);
		if (assetId == null) {
			result.setBlockReason(EpisodeUnlockConstants.BLOCK_NOT_ON_SHELF);
			return result;
		}
		DramaAssetEntity asset = dramaAssetDao.findOnShelfById(assetId);
		if (asset == null) {
			result.setBlockReason(EpisodeUnlockConstants.BLOCK_NOT_ON_SHELF);
			return result;
		}
		return resolve(uid, asset);
	}

	/**
	 * 已加载 asset 时复用。
	 */
	public EpisodePlayAccessResult resolve(Integer uid, DramaAssetEntity asset) {
		EpisodePlayAccessResult result = new EpisodePlayAccessResult();
		if (asset == null || asset.getId() == null) {
			result.setCanPlay(false);
			result.setBlockReason(EpisodeUnlockConstants.BLOCK_NOT_ON_SHELF);
			return result;
		}
		result.setAssetId(asset.getId());
		result.setDramaId(asset.getDramaId());
		result.setSetNum(asset.getSetNum());
		result.setUnlockType(asset.getUnlockType() == null
				? EpisodeUnlockTypeEnums.FREE.getIndex() : asset.getUnlockType());
		result.setPriceCoin(asset.getPriceCoin() == null ? 0 : asset.getPriceCoin());
		result.setPriceYuan(asset.getPriceYuan());

		DramaEntity drama = asset.getDramaId() == null ? null : dramaDao.selectById(asset.getDramaId());
		boolean memberDrama = drama != null
				&& Integer.valueOf(EpisodeUnlockConstants.MEMBER_DRAMA_YES).equals(drama.getIsMemberDrama());
		result.setMemberDrama(memberDrama);

		boolean member = uid != null && userMemberDao.findActiveByUid(uid) != null;
		result.setMember(member);

		// 已解锁记录优先
		if (uid != null) {
			UserEpisodeUnlockEntity unlock = userEpisodeUnlockDao.findByUidAndAssetId(uid, asset.getId());
			if (unlock != null) {
				result.setUnlocked(true);
				result.setUnlockWay(unlock.getUnlockWay());
				result.setCanPlay(true);
				result.setBlockReason(null);
				return result;
			}
		}

		// 会员短剧：仅 VIP 可播
		if (memberDrama) {
			if (member) {
				result.setCanPlay(true);
				result.setBlockReason(null);
				return result;
			}
			result.setCanPlay(false);
			result.setBlockReason(uid == null
					? EpisodeUnlockConstants.BLOCK_NEED_LOGIN
					: EpisodeUnlockConstants.BLOCK_NEED_MEMBER);
			return result;
		}

		Integer unlockType = result.getUnlockType();
		if (EpisodeUnlockTypeEnums.isFree(unlockType)) {
			result.setCanPlay(true);
			result.setBlockReason(null);
			return result;
		}
		if (EpisodeUnlockTypeEnums.isAd(unlockType)) {
			// VIP 免广告
			if (member) {
				result.setCanPlay(true);
				result.setBlockReason(null);
				return result;
			}
			result.setCanPlay(false);
			result.setBlockReason(uid == null
					? EpisodeUnlockConstants.BLOCK_NEED_LOGIN
					: EpisodeUnlockConstants.BLOCK_NEED_AD);
			return result;
		}
		if (EpisodeUnlockTypeEnums.isPay(unlockType)) {
			result.setCanPlay(false);
			result.setBlockReason(uid == null
					? EpisodeUnlockConstants.BLOCK_NEED_LOGIN
					: EpisodeUnlockConstants.BLOCK_NEED_PAY);
			return result;
		}

		// 未知类型按免费
		result.setCanPlay(true);
		result.setBlockReason(null);
		return result;
	}

	/** 是否有效会员 */
	public boolean isActiveMember(Integer uid) {
		return uid != null && userMemberDao.findActiveByUid(uid) != null;
	}
}
