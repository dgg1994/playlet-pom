package com.playlet.internal.service.impl;

import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.dao.drama.DramaAuditStepDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAuditStepEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.enums.DramaAppealStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStepStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStepTypeEnums;
import com.playlet.internal.enums.DramaAssetShelfStatusEnums;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.service.DramaAuditService;
import com.playlet.internal.service.RankAlgoService;
import com.playlet.internal.utils.GenericityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class DramaAuditServiceImpl implements DramaAuditService {

	private static final String AI_PASS_REMARK = "图片无违规，简介文字无违规，标签与简介匹配。";

	@Autowired
	private DramaDao dramaDao;
	@Autowired
	private DramaAuditStepDao dramaAuditStepDao;
	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private RankAlgoService rankAlgoService;

	@Override
	public void initAuditSteps(Integer dramaId) {
		try {
			if (dramaId == null) {
				return;
			}
			dramaAuditStepDao.deleteByDramaId(dramaId);
			Date now = new Date();

			DramaAuditStepEntity ai = new DramaAuditStepEntity();
			ai.setDramaId(dramaId);
			ai.setStepType(DramaAssetAuditStepTypeEnums.AI.getCode());
			ai.setStatus(DramaAssetAuditStepStatusEnums.PASS.getCode());
			ai.setHandlerId(0);
			ai.setHandleRemark(AI_PASS_REMARK);
			ai.setHandleTime(now);
			safeSetDate(ai);
			dramaAuditStepDao.insert(ai);

			DramaAuditStepEntity groupA = new DramaAuditStepEntity();
			groupA.setDramaId(dramaId);
			groupA.setStepType(DramaAssetAuditStepTypeEnums.GROUP_A.getCode());
			groupA.setStatus(DramaAssetAuditStepStatusEnums.PENDING.getCode());
			safeSetDate(groupA);
			dramaAuditStepDao.insert(groupA);

			DramaAuditStepEntity groupB = new DramaAuditStepEntity();
			groupB.setDramaId(dramaId);
			groupB.setStepType(DramaAssetAuditStepTypeEnums.GROUP_B.getCode());
			groupB.setStatus(DramaAssetAuditStepStatusEnums.PENDING.getCode());
			safeSetDate(groupB);
			dramaAuditStepDao.insert(groupB);

			refreshAggregateAndAutoShelf(dramaId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void refreshAggregateAndAutoShelf(Integer dramaId) {
		try {
			if (dramaId == null) {
				return;
			}
			DramaEntity drama = dramaDao.selectById(dramaId);
			if (drama == null) {
				return;
			}
			List<DramaAuditStepEntity> steps = dramaAuditStepDao.findByDramaId(dramaId);
			if (steps == null || steps.isEmpty()) {
				return;
			}
			Integer ai = null;
			Integer a = null;
			Integer b = null;
			String rejectReason = null;
			for (DramaAuditStepEntity step : steps) {
				if (step.getStepType() == null) {
					continue;
				}
				if (step.getStepType().equals(DramaAssetAuditStepTypeEnums.AI.getCode())) {
					ai = step.getStatus();
				} else if (step.getStepType().equals(DramaAssetAuditStepTypeEnums.GROUP_A.getCode())) {
					a = step.getStatus();
				} else if (step.getStepType().equals(DramaAssetAuditStepTypeEnums.GROUP_B.getCode())) {
					b = step.getStatus();
				}
				if (step.getStatus() != null && step.getStatus().equals(DramaAssetAuditStepStatusEnums.REJECT.getCode())
						&& rejectReason == null) {
					rejectReason = step.getHandleRemark();
				}
			}
			Date now = new Date();
			drama.setGmtModified(now);

			boolean anyReject = isReject(ai) || isReject(a) || isReject(b);
			boolean allPass = isPass(ai) && isPass(a) && isPass(b);
			boolean wasAppealing = isAppealing(drama);
			if (anyReject) {
				drama.setAuditStatus(DramaAssetAuditStatusEnums.REJECTED.getCode());
				drama.setAuditRejectReason(rejectReason);
				drama.setAuditPassTime(null);
				// 申诉中再驳回 → 3，离开申诉页签
				if (wasAppealing) {
					drama.setAppealStatus(DramaAppealStatusEnums.APPEAL_REJECT.getCode());
				}
				dramaDao.updateById(drama);
				forceUnshelfDramaAndEpisodes(dramaId);
				return;
			}
			if (allPass) {
				// 过审 ≠ 上架：仅标记审核通过，上架由集上架 sync 推导
				drama.setAuditStatus(DramaAssetAuditStatusEnums.APPROVED.getCode());
				drama.setAuditRejectReason(null);
				drama.setAuditPassTime(now);
				if (wasAppealing) {
					drama.setAppealStatus(DramaAppealStatusEnums.APPEAL_PASS.getCode());
				}
				dramaDao.updateById(drama);
				syncDramaShelfByEpisodes(dramaId);
				return;
			}
			// 申诉再审过程中保持 4，勿回写成审核中
			if (wasAppealing) {
				drama.setAuditStatus(DramaAssetAuditStatusEnums.APPEALING.getCode());
			} else {
				drama.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
			}
			drama.setAuditRejectReason(null);
			drama.setAuditPassTime(null);
			dramaDao.updateById(drama);
			forceUnshelfDramaAndEpisodes(dramaId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void syncDramaShelfByEpisodes(Integer dramaId) {
		try {
			if (dramaId == null) {
				return;
			}
			DramaEntity drama = dramaDao.selectById(dramaId);
			if (drama == null) {
				return;
			}
			Integer onShelfCount = dramaAssetDao.countOnShelfByDramaId(dramaId);
			boolean hasOnShelf = onShelfCount != null && onShelfCount > 0;
			boolean dramaApproved = drama.getAuditStatus() != null
					&& drama.getAuditStatus().equals(DramaAssetAuditStatusEnums.APPROVED.getCode());
			Date now = new Date();
			drama.setGmtModified(now);

			if (hasOnShelf && dramaApproved) {
				boolean alreadyOn = drama.getShelfStatus() != null
						&& drama.getShelfStatus().equals(DramaAssetShelfStatusEnums.ON.getCode())
						&& VerifyStateEnums.AVAILABLE_NOW.getIndex().equals(drama.getVerifyStatus());
				drama.setShelfStatus(DramaAssetShelfStatusEnums.ON.getCode());
				drama.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
				if (drama.getShelfTime() == null) {
					drama.setShelfTime(now);
				}
				dramaDao.updateById(drama);
				if (!alreadyOn) {
					try {
						rankAlgoService.refreshNewBoard();
					} catch (Exception e) {
						log.warn("refresh new board after drama shelf sync failed dramaId={}: {}",
								dramaId, e.getMessage());
					}
				}
				return;
			}

			drama.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			drama.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
			drama.setShelfTime(null);
			dramaDao.updateById(drama);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void forceUnshelfDramaAndEpisodes(Integer dramaId) {
		try {
			if (dramaId == null) {
				return;
			}
			dramaAssetDao.unshelfAllByDramaId(dramaId);
			DramaEntity drama = dramaDao.selectById(dramaId);
			if (drama == null) {
				return;
			}
			drama.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			drama.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
			drama.setShelfTime(null);
			drama.setGmtModified(new Date());
			dramaDao.updateById(drama);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void forceShelfDramaAndApprovedEpisodes(Integer dramaId) {
		try {
			if (dramaId == null) {
				return;
			}
			// 批量上架已过审集，再推导剧级 verifyStatus / shelfStatus
			int shelved = dramaAssetDao.shelfApprovedByDramaId(dramaId);
			log.info("force shelf drama approved episodes dramaId={} shelvedCount={}", dramaId, shelved);
			syncDramaShelfByEpisodes(dramaId);
		} catch (Exception e) {
			log.error("force shelf drama failed dramaId={}", dramaId, e);
			throw new RuntimeException(e);
		}
	}

	private static boolean isPass(Integer status) {
		return status != null && status.equals(DramaAssetAuditStepStatusEnums.PASS.getCode());
	}

	private static boolean isReject(Integer status) {
		return status != null && status.equals(DramaAssetAuditStepStatusEnums.REJECT.getCode());
	}

	/** 申诉再审中：audit_status=4，或历史数据仅写了 appeal_status=1。 */
	private static boolean isAppealing(DramaEntity drama) {
		if (drama == null) {
			return false;
		}
		if (DramaAssetAuditStatusEnums.isAppealing(drama.getAuditStatus())) {
			return true;
		}
		return drama.getAppealStatus() != null
				&& drama.getAppealStatus().equals(DramaAppealStatusEnums.APPEALING.getCode());
	}

	private void safeSetDate(Object entity) {
		try {
			GenericityUtil.setDate(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
