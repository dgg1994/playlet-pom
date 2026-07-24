package com.playlet.internal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.playlet.internal.api.response.WelfareHomeRespEntity;
import com.playlet.internal.api.response.WelfareTaskItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.UserWelfareProgressDao;
import com.playlet.internal.dao.welfare.WelfareTaskDao;
import com.playlet.internal.dao.welfare.WelfareTaskI18nDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.UserWelfareProgressEntity;
import com.playlet.internal.entity.welfare.WelfareTaskEntity;
import com.playlet.internal.entity.welfare.WelfareTaskI18nEntity;
import com.playlet.internal.enums.WelfareCycleTypeEnums;
import com.playlet.internal.enums.WelfareProgressStatusEnums;
import com.playlet.internal.service.SignInService;
import com.playlet.internal.service.WatchGiftService;
import com.playlet.internal.service.WelfareTaskApiService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WelfareTaskApiServiceImpl extends BaseApiService implements WelfareTaskApiService {

	@Autowired
	private WelfareTaskDao welfareTaskDao;
	@Autowired
	private WelfareTaskI18nDao welfareTaskI18nDao;
	@Autowired
	private UserWelfareProgressDao userWelfareProgressDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private SignInService signInService;
	@Autowired
	private WatchGiftService watchGiftService;

	@Override
	public ResponseBase home(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		WelfareHomeRespEntity resp = new WelfareHomeRespEntity();
		AppAccountEntity account = appAccountDao.findByUid(uid);
		resp.setCoinBalance(account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance());
		resp.setSignIn(signInService.buildHomeSummary(uid));
		resp.setWatchGift(watchGiftService.buildHomeSummary(uid));
		resp.setTasks(buildTaskItems(uid));
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase tasks(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		return setResultSuccess(buildTaskItems(uid), I18nUtil.getMessage("base_success"));
	}

	/**
	 * 构建任务项
	 * @param uid 用户ID
	 * @return 任务项
	 */
	private List<WelfareTaskItemEntity> buildTaskItems(Integer uid) {
		List<WelfareTaskEntity> tasks = welfareTaskDao.findEnabledList();
		if (tasks == null) {
			tasks = new ArrayList<>();
		}
		String language = LanguageContext.getLanguage();
		List<WelfareTaskItemEntity> items = new ArrayList<>();
		for (WelfareTaskEntity task : tasks) {
			String bizDate = resolveBizDate(task.getCycleType());
			UserWelfareProgressEntity progress = userWelfareProgressDao.findOne(uid, task.getId(), bizDate);
			if (progress != null) {
				refreshExpired(progress);
			}
			WelfareTaskItemEntity item = new WelfareTaskItemEntity();
			item.setTaskId(task.getId());
			item.setTaskCode(task.getTaskCode());
			item.setTaskIcon(task.getTaskIcon());
			item.setRewardCoin(task.getRewardCoin());
			item.setAdBoostCoin(task.getAdBoostCoin());
			item.setCycleType(task.getCycleType());
			item.setTargetCount(task.getTargetCount());
			item.setAutoClaim(task.getAutoClaim());
			item.setActionType(parseActionType(task.getExtraConfig()));
			log.info("福利首页语言:{}，taskId:{}",language,task.getId());
			WelfareTaskI18nEntity byTaskIdAndLangue = welfareTaskI18nDao.findByTaskIdAndLangue(task.getId(), language);
			if (byTaskIdAndLangue != null){
				item.setTaskName(byTaskIdAndLangue.getTaskName());
				item.setTaskDesc(byTaskIdAndLangue.getTaskDesc());
			}
			if (progress == null) {
				item.setProgress(0);
				item.setProgressStatus(WelfareProgressStatusEnums.NOT_ACCEPTED.getCode());
				item.setAccepted(false);
			} else {
				item.setAccepted(true);
				item.setProgress(progress.getProgress() == null ? 0 : progress.getProgress());
				item.setProgressStatus(progress.getProgressStatus());
				if (progress.getTarget() != null) {
					item.setTargetCount(progress.getTarget());
				}
			}
			items.add(item);
		}
		return items;
	}

	/**
	 * 刷新进度过期状态
	 * @param progress  行
	 */
	private void refreshExpired(UserWelfareProgressEntity progress) {
		if (progress == null || progress.getExpireTime() == null) {
			return;
		}
		Integer status = progress.getProgressStatus();
		if (status != null && (status == WelfareProgressStatusEnums.CLAIMED.getCode()
				|| status == WelfareProgressStatusEnums.EXPIRED.getCode()
				|| status == WelfareProgressStatusEnums.ABANDONED.getCode())) {
			return;
		}
		if (progress.getExpireTime().before(new Date())) {
			progress.setProgressStatus(WelfareProgressStatusEnums.EXPIRED.getCode());
			try {
				userWelfareProgressDao.updateClaim(progress.getId(), WelfareProgressStatusEnums.EXPIRED.getCode(),
						progress.getClaimTime());
			} catch (Exception e) {
				log.warn("refreshExpired failed id={}: {}", progress.getId(), e.getMessage());
			}
		}
	}

	/**
	 * 解析任务周期
	 * @param cycleType 周期类型
	 * @return 业务日期
	 */
	static String resolveBizDate(Integer cycleType) {
		LocalDate today = LocalDate.now();
		WelfareCycleTypeEnums type = WelfareCycleTypeEnums.fromCode(cycleType);
		if (type == null) {
			type = WelfareCycleTypeEnums.DAILY;
		}
		switch (type) {
			case ONCE:
				return "";
			case WEEKLY:
				WeekFields wf = WeekFields.ISO;
				int week = today.get(wf.weekOfWeekBasedYear());
				int year = today.get(wf.weekBasedYear());
				return String.format("%d-W%02d", year, week);
			case MONTHLY:
				return String.format("%d-%02d", today.getYear(), today.getMonthValue());
			case DAILY:
			default:
				return today.toString();
		}
	}

	/**
	 * 解析任务行为类型
	 * @param extraConfig 额外配置
	 * @return 行为类型
	 */
	static String parseActionType(String extraConfig) {
		if (StringUtils.isEmpty(extraConfig)) {
			return null;
		}
		try {
			JSONObject json = JSON.parseObject(extraConfig);
			if (json == null) {
				return null;
			}
			String action = json.getString("actionType");
			return StringUtils.isEmpty(action) ? null : action.trim();
		} catch (Exception e) {
			return null;
		}
	}

}
