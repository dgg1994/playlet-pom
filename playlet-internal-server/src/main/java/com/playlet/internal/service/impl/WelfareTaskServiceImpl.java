package com.playlet.internal.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.CoinLedgerItemEntity;
import com.playlet.internal.api.response.WelfareHomeRespEntity;
import com.playlet.internal.api.response.WelfareTaskItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.welfare.*;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.welfare.*;
import com.playlet.internal.enums.CoinBizTypeEnums;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.enums.WelfareCycleTypeEnums;
import com.playlet.internal.enums.WelfareProgressStatusEnums;
import com.playlet.internal.enums.WelfareTaskEnums;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import com.playlet.internal.service.SignInService;
import com.playlet.internal.service.WelfareTaskService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WelfareTaskServiceImpl extends BaseApiService implements WelfareTaskService {

	@Autowired
	private WelfareTaskDao welfareTaskDao;
	@Autowired
	private WelfareTaskI18nDao welfareTaskI18nDao;
	@Autowired
	private UserWelfareProgressDao userWelfareProgressDao;
	@Autowired
	private UserCoinLedgerDao userCoinLedgerDao;
	@Autowired
	private UserWelfareWatchLogDao userWelfareWatchLogDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private SignInService signInService;

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

	@Override
	public ResponseBase accept(@RequestParam Integer taskId, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (taskId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			WelfareTaskEntity task = welfareTaskDao.selectById(taskId);
			if (task == null || task.getStatus() == null
					|| !task.getStatus().equals(WelfareTaskEnums.STATUS_ENABLED.getIndex())) {
				return setResultError(I18nUtil.getMessage("welfare_task_null"));
			}
			String bizDate = resolveBizDate(task.getCycleType());
			UserWelfareProgressEntity exist = userWelfareProgressDao.findOne(uid, taskId, bizDate);
			if (exist != null) {
				refreshExpired(exist);
				Integer st = exist.getProgressStatus();
				if (st != null && st == WelfareProgressStatusEnums.CLAIMED.getCode()) {
					return setResultError(I18nUtil.getMessage("welfare_task_reward_claimed_period"));
				}
				if (st != null && st == WelfareProgressStatusEnums.EXPIRED.getCode()) {
					return setResultError(I18nUtil.getMessage("welfare_task_not_claimable"));
				}
				return setResultSuccess(I18nUtil.getMessage("welfare_task_accept_already"));
			}
			// 任务不存在，新增用户任务进度
			ensureProgress(uid, task, bizDate, null);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase claim(@RequestParam Integer taskId,
			@RequestParam(required = false, defaultValue = "false") Boolean adBoost,
			HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (taskId == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			WelfareTaskEntity task = welfareTaskDao.selectById(taskId);
			if (task == null || task.getStatus() == null
					|| !task.getStatus().equals(WelfareTaskEnums.STATUS_ENABLED.getIndex())) {
				return setResultError(I18nUtil.getMessage("welfare_task_null"));
			}
			String bizDate = resolveBizDate(task.getCycleType());
			UserWelfareProgressEntity progress = userWelfareProgressDao.findOne(uid, taskId, bizDate);
			if (progress == null) {
				return setResultError(I18nUtil.getMessage("welfare_task_not_claimable"));
			}
			refreshExpired(progress);
			if (progress.getProgressStatus() != null
					&& progress.getProgressStatus() == WelfareProgressStatusEnums.CLAIMED.getCode()) {
				return setResultError(I18nUtil.getMessage("welfare_task_claimed"));
			}
			if (progress.getProgressStatus() == null
					|| progress.getProgressStatus() != WelfareProgressStatusEnums.CLAIMABLE.getCode()) {
				return setResultError(I18nUtil.getMessage("welfare_task_not_claimable"));
			}
			boolean withAd = Boolean.TRUE.equals(adBoost);
			grantReward(uid, task, progress, withAd);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase ledger(String bizType, PageQueryHelperEntity page, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (page == null) {
			page = new PageQueryHelperEntity();
		}
		List<UserCoinLedgerEntity> rows;
		if (StringUtils.isEmpty(bizType)) {
			rows = userCoinLedgerDao.findByUid(uid);
		} else {
			rows = userCoinLedgerDao.findByUidAndBizType(uid, bizType.trim());
		}
		if (rows == null) {
			rows = new ArrayList<>();
		}
		List<UserCoinLedgerEntity> pageRows = GenericityUtil.Page(rows, page.getPageNumber(), page.getPageSize());
		List<CoinLedgerItemEntity> items = new ArrayList<>();
		for (UserCoinLedgerEntity row : pageRows) {
			CoinLedgerItemEntity item = new CoinLedgerItemEntity();
			item.setId(row.getId());
			item.setChangeAmt(row.getChangeAmt());
			item.setBalanceAfter(row.getBalanceAfter());
			item.setBizType(row.getBizType());
			item.setBizTypeLabel(CoinBizTypeEnums.getLableByName(row.getBizType()));
			item.setBizId(row.getBizId());
			item.setTaskCode(row.getTaskCode());
			item.setAdBoostFlag(row.getAdBoostFlag());
			item.setRemark(row.getRemark());
			item.setSetTime(row.getSetTime());
			items.add(item);
		}
		PageInfo<CoinLedgerItemEntity> pageInfo = new PageInfo<>(items);
		pageInfo.setTotal(rows.size());
		return setResultSuccess(pageInfo, I18nUtil.getMessage("base_success"));
	}

	@Override
	public void onAction(Integer uid, WelfareActionTypeEnums action, int delta, String extInfo) {
		if (uid == null || action == null || delta <= 0
				|| !action.isAutoProgress()) {
			return;
		}
		try {
			List<WelfareTaskEntity> tasks = welfareTaskDao.findEnabledList();
			if (tasks == null || tasks.isEmpty()) {
				return;
			}
			String actionName = action.getName();
			List<WelfareTaskEntity> matched = new ArrayList<>();
			for (WelfareTaskEntity task : tasks) {
				String taskAction = parseActionType(task.getExtraConfig());
				if (!actionName.equals(taskAction)) {
					continue;
				}
				String bizDate = resolveBizDate(task.getCycleType());
				UserWelfareProgressEntity progress = userWelfareProgressDao.findOne(uid, task.getId(), bizDate);
				// 未领取任务：不累计
				if (progress == null) {
					continue;
				}
				matched.add(task);
			}
			if (matched.isEmpty()) {
				return;
			}
			// 看剧去重：仅当本周期已领取过对应任务时才记账
			if (action == WelfareActionTypeEnums.WATCH) {
				if (!tryRecordWatch(uid, extInfo)) {
					return;
				}
			}
			for (WelfareTaskEntity task : matched) {
				increaseProgress(uid, task, delta, extInfo);
			}
		} catch (Exception e) {
			log.warn("welfare onAction failed uid={} action={}: {}", uid, action, e.getMessage());
		}
	}

	/**
	 * 记录观看记录
	 * @param uid 用户ID
	 * @param extInfo 扩展信息
	 * @return
	 * @throws Exception
	 */
	private boolean tryRecordWatch(Integer uid, String extInfo) throws Exception {
		JSONObject json = parseExt(extInfo);
		Integer dramaId = json.getInteger("dramaId");
		String episodeId = json.getString("episodeId");
		if (dramaId == null || StringUtils.isEmpty(episodeId)) {
			return false;
		}
		String bizDate = resolveBizDate(WelfareCycleTypeEnums.DAILY.getCode());
		UserWelfareWatchLogEntity logRow = new UserWelfareWatchLogEntity();
		logRow.setUid(uid);
		logRow.setDramaId(dramaId);
		logRow.setEpisodeId(episodeId.trim());
		logRow.setBizDate(bizDate);
		logRow.setSetTime(new Date());
		return userWelfareWatchLogDao.insertIgnore(logRow) > 0;
	}

	/**
	 * 累计进度
	 * @param uid 用户ID
	 * @param task 任务
	 * @param delta 增加进度
	 * @param extInfo 扩展信息
	 * @throws Exception
	 */
	private void increaseProgress(Integer uid, WelfareTaskEntity task, int delta, String extInfo)
			throws Exception {
		String bizDate = resolveBizDate(task.getCycleType());
		// 必须先领取任务（accept）才会有进度行；此处不再自动创建
		UserWelfareProgressEntity progress = userWelfareProgressDao.findOne(uid, task.getId(), bizDate);
		if (progress == null) {
			return;
		}
		refreshExpired(progress);
		Integer status = progress.getProgressStatus();
		if (WelfareProgressStatusEnums.isTerminal(status)) {
			return;
		}
		int target = progress.getTarget() == null ? 1 : progress.getTarget();
		int cur = progress.getProgress() == null ? 0 : progress.getProgress();
		int next = Math.min(cur + delta, target);
		int nextStatus = next >= target
				? WelfareProgressStatusEnums.CLAIMABLE.getCode()
				: WelfareProgressStatusEnums.DOING.getCode();
		progress.setProgress(next);
		progress.setProgressStatus(nextStatus);
		if (!StringUtils.isEmpty(extInfo)) {
			progress.setExtInfo(extInfo);
		}
		GenericityUtil.updateDate(progress);
		userWelfareProgressDao.updateProgress(progress.getId(), next, nextStatus);

		if (nextStatus == WelfareProgressStatusEnums.CLAIMABLE.getCode()
				&& task.getAutoClaim() != null && task.getAutoClaim() == 1) {
			grantReward(uid, task, progress, false);
		}
	}

	/**
	 * 确保进度行
	 * @param uid 用户ID
	 * @param task 任务
	 * @param bizDate 业务日期
	 * @param extInfo 额外信息
	 * @return 进度行
	 * @throws Exception 创建失败
	 */
	private UserWelfareProgressEntity ensureProgress(Integer uid, WelfareTaskEntity task, String bizDate,
			String extInfo) throws Exception {
		UserWelfareProgressEntity exist = userWelfareProgressDao.findOne(uid, task.getId(), bizDate);
		if (exist != null) {
			return exist;
		}
		UserWelfareProgressEntity row = new UserWelfareProgressEntity();
		row.setUid(uid);
		row.setTaskId(task.getId());
		row.setBizDate(bizDate == null ? "" : bizDate);
		row.setProgress(0);
		row.setTarget(task.getTargetCount() == null || task.getTargetCount() < 1 ? 1 : task.getTargetCount());
		row.setProgressStatus(WelfareProgressStatusEnums.DOING.getCode());
		row.setExtInfo(extInfo);
		if (task.getExpireDays() != null && task.getExpireDays() > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, task.getExpireDays());
			row.setExpireTime(cal.getTime());
		}
		GenericityUtil.setDate(row);
		try {
			userWelfareProgressDao.insert(row);
		} catch (DuplicateKeyException e) {
			UserWelfareProgressEntity again = userWelfareProgressDao.findOne(uid, task.getId(), bizDate);
			if (again != null) {
				return again;
			}
			throw e;
		}
		return row;
	}

	/**
	 * 发放奖励
	 * @param uid 用户ID
	 * @param task 任务
	 * @param progress 进度行
	 * @param withAdBoost 是否发放广告加赠
	 * @throws Exception
	 */
	private void grantReward(Integer uid, WelfareTaskEntity task, UserWelfareProgressEntity progress,
			boolean withAdBoost) throws Exception {
		String bizDate = progress.getBizDate() == null ? "" : progress.getBizDate();
		String code = StringUtils.isEmpty(task.getTaskCode()) ? String.valueOf(task.getId()) : task.getTaskCode();
		int base = task.getRewardCoin() == null ? 0 : Math.max(0, task.getRewardCoin());
		if (base > 0) {
			creditCoin(uid, base, CoinBizTypeEnums.TASK_REWARD.getName(),
					code + ":" + bizDate + ":BASE", code, 0, "task reward");
		}
		int ad = task.getAdBoostCoin() == null ? 0 : Math.max(0, task.getAdBoostCoin());
		if (withAdBoost && ad > 0) {
			creditCoin(uid, ad, CoinBizTypeEnums.AD_BOOST.getName(),
					code + ":" + bizDate + ":AD", code, 1, "ad boost");
		}
		Date now = new Date();
		userWelfareProgressDao.updateClaim(progress.getId(), WelfareProgressStatusEnums.CLAIMED.getCode(), now);
		progress.setProgressStatus(WelfareProgressStatusEnums.CLAIMED.getCode());
		progress.setClaimTime(now);
	}

	/**
	 * 积分流水
	 */
	private void creditCoin(Integer uid, int amt, String bizType, String bizId, String taskCode,
			int adBoostFlag, String remark) throws Exception {
		if (amt <= 0) {
			return;
		}
		UserCoinLedgerEntity exist = userCoinLedgerDao.findByBiz(uid, bizType, bizId);
		if (exist != null) {
			return;
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		long before = account == null || account.getCoinBalance() == null ? 0L : account.getCoinBalance();
		UserCoinLedgerEntity ledger = new UserCoinLedgerEntity();
		ledger.setUid(uid);
		ledger.setChangeAmt(amt);
		ledger.setBalanceBefore(before);
		ledger.setBalanceAfter(before + amt);
		ledger.setBizType(bizType);
		ledger.setBizId(bizId);
		ledger.setTaskCode(taskCode == null ? "" : taskCode);
		ledger.setAdBoostFlag(adBoostFlag);
		ledger.setRemark(remark == null ? "" : remark);
		GenericityUtil.setDate(ledger);
		try {
			userCoinLedgerDao.insert(ledger);
		} catch (DuplicateKeyException e) {
			return;
		}
		appAccountDao.addCoinBalance(uid, amt);
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

	/**
	 * 解析任务行为参数
	 * @param extInfo 额外信息
	 * @return
	 */
	private static JSONObject parseExt(String extInfo) {
		if (StringUtils.isEmpty(extInfo)) {
			return new JSONObject();
		}
		try {
			JSONObject json = JSON.parseObject(extInfo);
			return json == null ? new JSONObject() : json;
		} catch (Exception e) {
			return new JSONObject();
		}
	}
}
