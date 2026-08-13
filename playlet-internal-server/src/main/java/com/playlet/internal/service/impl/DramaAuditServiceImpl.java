package com.playlet.internal.service.impl;

import com.playlet.internal.dao.drama.DramaAuditStepDao;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.entity.drama.DramaAuditStepEntity;
import com.playlet.internal.entity.drama.DramaEntity;
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
			if (anyReject) {
				drama.setAuditStatus(DramaAssetAuditStatusEnums.REJECTED.getCode());
				drama.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
				drama.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
				drama.setAuditRejectReason(rejectReason);
				drama.setShelfTime(null);
				drama.setAuditPassTime(null);
				dramaDao.updateById(drama);
				return;
			}
			if (allPass) {
				drama.setAuditStatus(DramaAssetAuditStatusEnums.APPROVED.getCode());
				drama.setAuditRejectReason(null);
				drama.setAuditPassTime(now);
				drama.setShelfStatus(DramaAssetShelfStatusEnums.ON.getCode());
				drama.setShelfTime(now);
				drama.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
				dramaDao.updateById(drama);
				try {
					rankAlgoService.refreshNewBoard();
				} catch (Exception e) {
					log.warn("refresh new board after drama audit shelf failed dramaId={}: {}", dramaId, e.getMessage());
				}
				return;
			}
			drama.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
			drama.setAuditRejectReason(null);
			drama.setAuditPassTime(null);
			drama.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
			drama.setShelfTime(null);
			drama.setVerifyStatus(VerifyStateEnums.REMOVED_SHELVES.getIndex());
			dramaDao.updateById(drama);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isPass(Integer status) {
		return status != null && status.equals(DramaAssetAuditStepStatusEnums.PASS.getCode());
	}

	private static boolean isReject(Integer status) {
		return status != null && status.equals(DramaAssetAuditStepStatusEnums.REJECT.getCode());
	}

	private void safeSetDate(Object entity) {
		try {
			GenericityUtil.setDate(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
