package com.playlet.oversea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.playlet.oversea.api.response.WelfareHomeRespEntity;
import com.playlet.oversea.api.response.WelfareTaskItemEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.account.AppAccountDao;
import com.playlet.oversea.dao.welfare.UserWelfareProgressDao;
import com.playlet.oversea.dao.welfare.WelfareTaskDao;
import com.playlet.oversea.dao.welfare.WelfareTaskI18nDao;
import com.playlet.oversea.entity.account.AppAccountEntity;
import com.playlet.oversea.entity.welfare.UserWelfareProgressEntity;
import com.playlet.oversea.entity.welfare.WelfareTaskEntity;
import com.playlet.oversea.entity.welfare.WelfareTaskI18nEntity;
import com.playlet.oversea.enums.WelfareCycleTypeEnums;
import com.playlet.oversea.enums.WelfareProgressStatusEnums;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.service.SignInService;
import com.playlet.oversea.service.WatchGiftService;
import com.playlet.oversea.service.WelfareTaskApiService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	@Autowired
	private MediaUrlService mediaUrlService;

	@Override
	public ResponseBase home(HttpServletRequest request) {
		WelfareHomeRespEntity resp = new WelfareHomeRespEntity();
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid != null) {
			AppAccountEntity account = appAccountDao.findByUid(uid);
			resp.setCoinBalance(account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance());
		}else {
			resp.setCoinBalance(0L);
		}
		resp.setSignIn(signInService.buildHomeSummary(uid));
		resp.setWatchGift(watchGiftService.buildHomeSummary(uid));
		resp.setTasks(buildTaskItems(uid));
		return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
	}

	@Override
	public ResponseBase tasks(HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(Constants.HTTP_RES_CODE_403,I18nUtil.getMessage("login_required"));
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
		Map<Integer, String> bizDateByTaskId = new HashMap<>();
		Set<String> bizDates = new HashSet<>();
		List<Integer> taskIds = new ArrayList<>(tasks.size());
		for (WelfareTaskEntity task : tasks) {
			if (task == null || task.getId() == null) {
				continue;
			}
			taskIds.add(task.getId());
			String bizDate = resolveBizDate(task.getCycleType());
			bizDateByTaskId.put(task.getId(), bizDate);
			bizDates.add(bizDate);
		}

		Map<String, UserWelfareProgressEntity> progressMap = loadProgressMap(uid, bizDates);
		Map<Integer, WelfareTaskI18nEntity> i18nMap = loadI18nMap(taskIds, language);

		List<WelfareTaskItemEntity> items = new ArrayList<>();
		for (WelfareTaskEntity task : tasks) {
			if (task == null || task.getId() == null) {
				continue;
			}
			String bizDate = bizDateByTaskId.get(task.getId());
			UserWelfareProgressEntity progress = progressMap.get(task.getId() + "#" + bizDate);
			if (progress != null) {
				refreshExpired(progress);
			}
			WelfareTaskItemEntity item = new WelfareTaskItemEntity();
			item.setTaskId(task.getId());
			item.setTaskCode(task.getTaskCode());
			item.setTaskIcon(mediaUrlService.sign(task.getTaskIcon()));
			item.setRewardCoin(task.getRewardCoin());
			item.setAdBoostCoin(task.getAdBoostCoin());
			item.setCycleType(task.getCycleType());
			item.setTargetCount(task.getTargetCount());
			item.setAutoClaim(task.getAutoClaim());
			item.setActionType(parseActionType(task.getExtraConfig()));
			log.info("福利首页语言:{}，taskId:{}", language, task.getId());
			WelfareTaskI18nEntity byTaskIdAndLangue = i18nMap.get(task.getId());
			if (byTaskIdAndLangue != null) {
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
	 * 加载进度
	 * @param uid 用户ID
	 * @param bizDates 业务日期
	 * @return 进度
	 */
	private Map<String, UserWelfareProgressEntity> loadProgressMap(Integer uid, Set<String> bizDates) {
		if (uid == null || bizDates == null || bizDates.isEmpty()) {
			return Collections.emptyMap();
		}
		List<UserWelfareProgressEntity> list = userWelfareProgressDao.findByUidAndBizDates(uid,
				new ArrayList<>(bizDates));
		if (list == null || list.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, UserWelfareProgressEntity> map = new HashMap<>();
		for (UserWelfareProgressEntity p : list) {
			if (p == null || p.getTaskId() == null || p.getBizDate() == null) {
				continue;
			}
			map.put(p.getTaskId() + "#" + p.getBizDate(), p);
		}
		return map;
	}

	/**
	 * 加载国际化
	 * @param taskIds 任务ID
	 * @param language 语言
	 * @return 国际化
	 */
	private Map<Integer, WelfareTaskI18nEntity> loadI18nMap(List<Integer> taskIds, String language) {
		if (taskIds == null || taskIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<WelfareTaskI18nEntity> list = welfareTaskI18nDao.findByTaskIdsAndLangue(taskIds, language);
		if (list == null || list.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<Integer, WelfareTaskI18nEntity> map = new HashMap<>(list.size());
		for (WelfareTaskI18nEntity row : list) {
			if (row != null && row.getTaskId() != null) {
				map.putIfAbsent(row.getTaskId(), row);
			}
		}
		return map;
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
