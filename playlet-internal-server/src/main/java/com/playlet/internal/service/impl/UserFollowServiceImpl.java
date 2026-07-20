package com.playlet.internal.service.impl;

import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.UserFollowItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.account.AppAccountDao;
import com.playlet.internal.dao.account.UserFollowDao;
import com.playlet.internal.entity.account.AppAccountEntity;
import com.playlet.internal.entity.account.UserFollowEntity;
import com.playlet.internal.service.UserFollowService;
import com.playlet.internal.service.WelfareTaskService;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@RestController
@CrossOrigin
public class UserFollowServiceImpl extends BaseApiService implements UserFollowService {

	@Autowired
	private UserFollowDao userFollowDao;
	@Autowired
	private AppAccountDao appAccountDao;
	@Autowired
	private WelfareTaskService welfareTaskService;

	@Override
	public ResponseBase followAdd(@RequestParam Integer followUid, HttpServletRequest request) {
		Integer uid = AppTokenUtil.resolveUid(request);
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("login_required"));
		}
		if (followUid == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (uid.equals(followUid)) {
			return setResultError(I18nUtil.getMessage("follow_self_forbidden"));
		}
		if (appAccountDao.findByUid(followUid) == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		if (userFollowDao.findOne(uid, followUid) != null) {
			return setResultError(I18nUtil.getMessage("follow_already"));
		}
		try {
			UserFollowEntity row = new UserFollowEntity();
			row.setUid(uid);
			row.setFollowUid(followUid);
			GenericityUtil.setDate(row);
			userFollowDao.insert(row);
			try {
				welfareTaskService.onAction(uid, WelfareActionTypeEnums.FOLLOW, 1,
						"{\"followUid\":\"" + followUid + "\"}");
			} catch (Exception e) {
				log.warn("welfare follow progress failed: {}", e.getMessage());
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResponseBase followCancel(@RequestParam Integer followUid, HttpServletRequest request) {
        try {
        	Integer uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (followUid == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            userFollowDao.deleteOne(uid, followUid);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase followingList(UserFollowEntity entity, HttpServletRequest request) {
        try {
            Integer targetUid = resolveTargetUid(entity == null ? null : entity.getUid(), request);
            if (targetUid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (entity == null) {
                entity = new UserFollowEntity();
            }
            List<UserFollowEntity> rows = userFollowDao.findFollowing(targetUid);
            return buildUserPage(rows, entity.getPageNumber(), entity.getPageSize(), request, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public ResponseBase fansList(UserFollowEntity entity, HttpServletRequest request) {
        try {
        	Integer targetUid = resolveTargetUid(entity == null ? null : entity.getUid(), request);
            if (targetUid == null) {
                return setResultError(I18nUtil.getMessage("login_required"));
            }
            if (entity == null) {
                entity = new UserFollowEntity();
            }
            List<UserFollowEntity> rows = userFollowDao.findFans(targetUid);
            return buildUserPage(rows, entity.getPageNumber(), entity.getPageSize(), request, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


	/**
	 * @param followingSide true=关注列表展示 followUid；false=粉丝列表展示 uid
	 */
	private ResponseBase buildUserPage(List<UserFollowEntity> rows, Integer pageNumber, Integer pageSize,
			HttpServletRequest request, boolean followingSide) {
		if (rows == null) {
			rows = new ArrayList<>();
		}
		List<UserFollowEntity> pageRows = GenericityUtil.Page(rows, pageNumber, pageSize);
		Integer viewer = AppTokenUtil.resolveUid(request);
		Map<Integer, AppAccountEntity> accountCache = new HashMap<>();
		Set<Integer> followedSet = loadFollowedSet(viewer, pageRows, followingSide);

		List<UserFollowItemEntity> items = new ArrayList<>();
		for (UserFollowEntity row : pageRows) {
			Integer otherUid = followingSide ? row.getFollowUid() : row.getUid();
			if (otherUid == null) {
				continue;
			}
			AppAccountEntity account = resolveAccount(otherUid, accountCache);
			UserFollowItemEntity item = new UserFollowItemEntity();
			item.setUid(otherUid);
			item.setNickname(displayName(account, otherUid));
			item.setAvatar(account == null ? null : account.getAvatar());
			item.setSetTime(row.getSetTime());
			item.setFollowed(followedSet.contains(otherUid));
			items.add(item);
		}
		PageInfo<UserFollowItemEntity> page = new PageInfo<>(items);
		page.setTotal(rows.size());
		return setResultSuccess(page, I18nUtil.getMessage("base_success"));
	}

	private Set<Integer> loadFollowedSet(Integer viewer, List<UserFollowEntity> pageRows, boolean followingSide) {
		Set<Integer> set = new HashSet<>();
		if (viewer == null || pageRows == null) {
			return set;
		}
		for (UserFollowEntity row : pageRows) {
			Integer otherUid = followingSide ? row.getFollowUid() : row.getUid();
			if (otherUid == null || viewer.equals(otherUid)) {
				continue;
			}
			if (userFollowDao.findOne(viewer, otherUid) != null) {
				set.add(otherUid);
			}
		}
		return set;
	}

	private Integer resolveTargetUid(Integer uidParam, HttpServletRequest request) {
		if (uidParam != null) {
			return uidParam;
		}
		return AppTokenUtil.resolveUid(request);
	}

	private AppAccountEntity resolveAccount(Integer uid, Map<Integer, AppAccountEntity> cache) {
		if (cache.containsKey(uid)) {
			return cache.get(uid);
		}
		AppAccountEntity account = appAccountDao.findByUid(uid);
		cache.put(uid, account);
		return account;
	}

	private String displayName(AppAccountEntity account, Integer uid) {
		if (account != null && StringUtils.isNotEmpty(account.getNickname())) {
			return account.getNickname();
		}
		if (account != null && StringUtils.isNotEmpty(account.getUserAccount())) {
			return account.getUserAccount();
		}
		return uid.toString();
	}
}
