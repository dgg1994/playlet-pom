package com.playlet.internal.service.impl;

import com.playlet.internal.dao.drama.DramaAssetAuditStepDao;
import com.playlet.internal.dao.drama.DramaAssetDao;
import com.playlet.internal.entity.drama.DramaAssetAuditStepEntity;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.enums.DramaAssetAuditStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStepStatusEnums;
import com.playlet.internal.enums.DramaAssetAuditStepTypeEnums;
import com.playlet.internal.enums.DramaAssetShelfStatusEnums;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.service.DramaAssetAuditService;
import com.playlet.internal.utils.GenericityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class DramaAssetAuditServiceImpl implements DramaAssetAuditService {

	@Autowired
	private DramaAssetDao dramaAssetDao;
	@Autowired
	private DramaAssetAuditStepDao dramaAssetAuditStepDao;

	@Override
	public void initAuditStepsOnRelease(Integer assetId, Integer dramaId) {
        try {
            dramaAssetAuditStepDao.deleteByAssetId(assetId);
            Date now = new Date();
            DramaAssetAuditStepEntity ai = new DramaAssetAuditStepEntity();
            ai.setAssetId(assetId);
            ai.setDramaId(dramaId);
            ai.setStepType(DramaAssetAuditStepTypeEnums.AI.getCode());
            ai.setStatus(DramaAssetAuditStepStatusEnums.PASS.getCode());
            ai.setHandlerId(0);
            ai.setHandleRemark("upload success auto pass");
            ai.setHandleTime(now);
            safeSetDate(ai);
            dramaAssetAuditStepDao.insert(ai);

            DramaAssetAuditStepEntity groupA = new DramaAssetAuditStepEntity();
            groupA.setAssetId(assetId);
            groupA.setDramaId(dramaId);
            groupA.setStepType(DramaAssetAuditStepTypeEnums.GROUP_A.getCode());
            groupA.setStatus(DramaAssetAuditStepStatusEnums.PENDING.getCode());
            safeSetDate(groupA);
            dramaAssetAuditStepDao.insert(groupA);

            DramaAssetAuditStepEntity groupB = new DramaAssetAuditStepEntity();
            groupB.setAssetId(assetId);
            groupB.setDramaId(dramaId);
            groupB.setStepType(DramaAssetAuditStepTypeEnums.GROUP_B.getCode());
            groupB.setStatus(DramaAssetAuditStepStatusEnums.PENDING.getCode());
            safeSetDate(groupB);
            dramaAssetAuditStepDao.insert(groupB);

            refreshAggregateAndAutoShelf(assetId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public void refreshAggregateAndAutoShelf(Integer assetId) {
        try {
            if (assetId == null) {
                return;
            }
            // 剧集信息
            DramaAssetEntity asset = dramaAssetDao.selectById(assetId);
            if (asset == null) {
                return;
            }
            // 审核步骤
            List<DramaAssetAuditStepEntity> steps = dramaAssetAuditStepDao.findByAssetId(assetId);
            if (steps == null || steps.isEmpty()) {
                return;
            }
            Integer ai = null;
            Integer a = null;
            Integer b = null;
            String rejectReason = null;
            // 获取审核步骤
            for (DramaAssetAuditStepEntity step : steps) {
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
            asset.setGmtModified(now);

            boolean anyReject = isReject(ai) || isReject(a) || isReject(b);
            boolean allPass = isPass(ai) && isPass(a) && isPass(b);
            // 驳回
            if (anyReject) {
                asset.setAuditStatus(DramaAssetAuditStatusEnums.REJECTED.getCode());
                asset.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
                asset.setVideoStatus(PublicEnums.ZERO.getIndex());
                asset.setAuditRejectReason(rejectReason);
                asset.setShelfTime(null);
                asset.setAuditPassTime(null);
                dramaAssetDao.updateById(asset);
                return;
            }
            // 通过
            if (allPass) {
                asset.setAuditStatus(DramaAssetAuditStatusEnums.APPROVED.getCode());
                asset.setAuditRejectReason(null);
                asset.setAuditPassTime(now);
                asset.setShelfStatus(DramaAssetShelfStatusEnums.ON.getCode());
                asset.setShelfTime(now);
                asset.setVideoStatus(PublicEnums.ONE.getIndex());
                dramaAssetDao.updateById(asset);
                return;
            }
            // 待审
            asset.setAuditStatus(DramaAssetAuditStatusEnums.UNDER_REVIEW.getCode());
            asset.setAuditRejectReason(null);
            asset.setAuditPassTime(null);
            asset.setShelfStatus(DramaAssetShelfStatusEnums.OFF.getCode());
            asset.setShelfTime(null);
            asset.setVideoStatus(PublicEnums.ZERO.getIndex());
            dramaAssetDao.updateById(asset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * 判断是否通过
	 *
	 * @param status
	 * @return
	 */
	private static boolean isPass(Integer status) {
		return status != null && status.equals(DramaAssetAuditStepStatusEnums.PASS.getCode());
	}

	/**
	 * 判断是否拒绝
	 * @param status
	 * @return
	 */
	private static boolean isReject(Integer status) {
		return status != null && status.equals(DramaAssetAuditStepStatusEnums.REJECT.getCode());
	}

	/**
	 * 安全设置时间
	 * @param entity
	 */
	private void safeSetDate(Object entity) {
		try {
			GenericityUtil.setDate(entity);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
