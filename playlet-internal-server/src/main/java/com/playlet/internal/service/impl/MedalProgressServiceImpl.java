package com.playlet.internal.service.impl;

import com.playlet.internal.dao.medal.MedalConfigDao;
import com.playlet.internal.dao.medal.MedalConfigI18nDao;
import com.playlet.internal.dao.medal.UserMedalDao;
import com.playlet.internal.dao.medal.UserMedalUnlockLogDao;
import com.playlet.internal.entity.medal.MedalConfigEntity;
import com.playlet.internal.entity.medal.MedalConfigI18nEntity;
import com.playlet.internal.entity.medal.UserMedalEntity;
import com.playlet.internal.entity.medal.UserMedalUnlockLogEntity;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.service.MedalProgressService;
import com.playlet.internal.service.PushNotifyService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.StringUtils;
import com.playlet.internal.utils.TransactionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MedalProgressServiceImpl implements MedalProgressService {

	@Autowired
	private MedalConfigDao medalConfigDao;
	@Autowired
	private MedalConfigI18nDao medalConfigI18nDao;
	@Autowired
	private UserMedalDao userMedalDao;
	@Autowired
	private UserMedalUnlockLogDao userMedalUnlockLogDao;
	@Autowired
	private PushNotifyService pushNotifyService;

	private static final String FALLBACK_LANGUE = "zh-cn";

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void onAction(Integer uid, WelfareActionTypeEnums action, int delta, String triggerRef) {
		if (uid == null || action == null || delta <= 0 || !action.isAutoProgress()) {
			return;
		}
		try {
			String ref = StringUtils.isEmpty(triggerRef) ? null : triggerRef.trim();
			// 观看去重：同一 triggerRef 只推进一次（覆盖同 action 下多枚勋章）
			if (action == WelfareActionTypeEnums.WATCH && !StringUtils.isEmpty(ref)
					&& userMedalUnlockLogDao.existsByUidActionRef(uid.longValue(), action.getName(), ref) > 0) {
				return;
			}

			// 获取勋章列表
			List<MedalConfigEntity> medals = medalConfigDao.findEnabledByActionType(action.getName());
			if (medals == null || medals.isEmpty()) {
				return;
			}
			for (MedalConfigEntity medal : medals) {
				increaseOne(uid.longValue(), medal, delta, action.getName(), ref);
			}
		} catch (Exception e) {
			log.warn("medal onAction failed uid={} action={}: {}", uid, action, e.getMessage());
			TransactionUtils.markRollbackOnly();
		}
	}

	/**
	 * 推进单个勋章进度
	 */
	private void increaseOne(Long uid, MedalConfigEntity medal, int delta, String actionName, String triggerRef)
			throws Exception {
		// 获取进度
		UserMedalEntity row = userMedalDao.findByUidAndMedalId(uid, medal.getId());
		if (row == null) {
			row = createProgress(uid, medal);
			if (row == null) {
				row = userMedalDao.findByUidAndMedalId(uid, medal.getId());
			}
		}
		if (row == null) {
			return;
		}
		if (row.getUnlocked() != null && row.getUnlocked() == 1) {
			return;
		}

		int before = row.getProgress() == null ? 0 : row.getProgress();
		int target = row.getTargetCount() == null
				? (medal.getTargetCount() == null ? 1 : medal.getTargetCount())
				: row.getTargetCount();
		int after = Math.min(before + delta, target);
		if (after <= before) {
			return;
		}

		boolean unlock = after >= target;
		Date now = new Date();
		row.setProgress(after);
		row.setGmtModified(now);
		if (unlock) {
			row.setUnlocked(1);
			row.setUnlockTime(now);
			row.setNotifyStatus(0);
		}
		userMedalDao.updateById(row);

		// 添加解锁日志
		UserMedalUnlockLogEntity logRow = new UserMedalUnlockLogEntity();
		logRow.setUid(uid);
		logRow.setMedalId(medal.getId());
		logRow.setMedalCode(medal.getMedalCode());
		logRow.setProgressBefore(before);
		logRow.setProgressAfter(after);
		logRow.setTriggerAction(actionName);
		logRow.setTriggerRef(triggerRef);
		logRow.setUnlockFlag(unlock ? 1 : 0);
		logRow.setSetTime(now);
		userMedalUnlockLogDao.insert(logRow);

		if (unlock) {
			try {
				String medalName = resolveMedalName(medal.getId());
				pushNotifyService.notifyMedalUnlock(uid.intValue(), medal.getId(), medalName);
			} catch (Exception e) {
				log.warn("medal unlock push failed uid={} medalId={}: {}", uid, medal.getId(), e.getMessage());
			}
		}
	}

	/**
	 * 解析勋章名称
	 */
	private String resolveMedalName(Integer medalId) {
		MedalConfigI18nEntity i18n = medalConfigI18nDao.findByMedalIdAndLangue(medalId, FALLBACK_LANGUE);
		if (i18n != null && !StringUtils.isEmpty(i18n.getMedalName())) {
			return i18n.getMedalName();
		}
		return medalConfigI18nDao.selectNameByMedalId(medalId, FALLBACK_LANGUE);
	}

	/**
	 * 创建进度
	 */
	private UserMedalEntity createProgress(Long uid, MedalConfigEntity medal) throws Exception {
		UserMedalEntity row = new UserMedalEntity();
		row.setUid(uid);
		row.setMedalId(medal.getId());
		row.setProgress(0);
		row.setTargetCount(medal.getTargetCount() == null ? 1 : medal.getTargetCount());
		row.setUnlocked(0);
		row.setNotifyStatus(0);
		GenericityUtil.setDate(row);
		try {
			userMedalDao.insert(row);
			return row;
		} catch (DuplicateKeyException e) {
			return null;
		}
	}
}
