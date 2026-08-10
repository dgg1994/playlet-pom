package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.playlet.oversea.api.response.UserFollowItemEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.account.AppAccountDao;
import com.playlet.oversea.dao.account.UserFollowDao;
import com.playlet.oversea.entity.account.AppAccountEntity;
import com.playlet.oversea.entity.account.UserFollowEntity;
import com.playlet.oversea.service.MedalProgressService;
import com.playlet.oversea.service.UserFollowService;
import com.playlet.oversea.service.WelfareTaskService;
import com.playlet.oversea.enums.WelfareActionTypeEnums;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
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
    @Autowired
    private MedalProgressService medalProgressService;

    @Override
    public ResponseBase followAdd(@RequestParam("followUid") Integer followUid, HttpServletRequest request) {
        Integer uid = AppTokenUtil.resolveUid(request);
        if (uid == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
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
            try {
                medalProgressService.onAction(uid, WelfareActionTypeEnums.FOLLOW, 1, String.valueOf(followUid));
            } catch (Exception e) {
                log.warn("medal follow progress failed: {}", e.getMessage());
            }
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase followCancel(@RequestParam("followUid") Integer followUid, HttpServletRequest request) {
        try {
            Integer uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
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
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
            }
            if (entity == null) {
                entity = new UserFollowEntity();
            }
            PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
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
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
            }
            if (entity == null) {
                entity = new UserFollowEntity();
            }
            PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
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
        // rows 已由 PageHelper 在 SQL 层完成分页；这里不再二次切片
        List<UserFollowEntity> pageRows = rows;
        PageInfo<UserFollowEntity> basePage = new PageInfo<>(pageRows);
        Integer viewer = AppTokenUtil.resolveUid(request);
        List<Integer> otherUids = collectOtherUids(pageRows, followingSide);
        Map<Integer, AppAccountEntity> accountCache = loadAccountMap(otherUids);
        Set<Integer> followedSet = loadFollowedSet(viewer, otherUids);

        List<UserFollowItemEntity> items = new ArrayList<>();
        for (UserFollowEntity row : pageRows) {
            Integer otherUid = followingSide ? row.getFollowUid() : row.getUid();
            if (otherUid == null) {
                continue;
            }
            AppAccountEntity account = accountCache.get(otherUid);
            UserFollowItemEntity item = new UserFollowItemEntity();
            item.setUid(otherUid);
            item.setNickname(displayName(account, otherUid));
            item.setAvatar(account == null ? null : account.getAvatar());
            item.setSetTime(row.getSetTime());
            item.setFollowed(followedSet.contains(otherUid));
            items.add(item);
        }
        PageInfo<UserFollowItemEntity> page = new PageInfo<>(items);
        // 继承 PageHelper 计算的总数/页数/hasNextPage
        page.setTotal(basePage.getTotal());
        page.setPageNum(basePage.getPageNum());
        page.setPageSize(basePage.getPageSize());
        page.setPages(basePage.getPages());
        page.setHasNextPage(basePage.isHasNextPage());
        page.setHasPreviousPage(basePage.isHasPreviousPage());
        return setResultSuccess(page, I18nUtil.getMessage("base_success"));
    }

    /**
     * 获取其他用户信息
     */
    private List<Integer> collectOtherUids(List<UserFollowEntity> pageRows, boolean followingSide) {
        List<Integer> uids = new ArrayList<>();
        if (pageRows == null) {
            return uids;
        }
        for (UserFollowEntity row : pageRows) {
            Integer otherUid = followingSide ? row.getFollowUid() : row.getUid();
            if (otherUid != null) {
                uids.add(otherUid);
            }
        }
        return uids;
    }

    /**
     * 获取用户信息
     */
    private Map<Integer, AppAccountEntity> loadAccountMap(List<Integer> uids) {
        if (uids == null || uids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> uniq = new ArrayList<>(new HashSet<>(uids));
        List<AppAccountEntity> list = appAccountDao.findByUids(uniq);
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, AppAccountEntity> map = new HashMap<>(list.size());
        for (AppAccountEntity a : list) {
            if (a != null && a.getId() != null) {
                map.put(a.getId(), a);
            }
        }
        return map;
    }

    /**
     * 获取已关注用户
     */
    private Set<Integer> loadFollowedSet(Integer viewer, List<Integer> otherUids) {
        Set<Integer> set = new HashSet<>();
        if (viewer == null || otherUids == null || otherUids.isEmpty()) {
            return set;
        }
        List<Integer> candidates = new ArrayList<>();
        for (Integer otherUid : otherUids) {
            if (otherUid != null && !viewer.equals(otherUid)) {
                candidates.add(otherUid);
            }
        }
        if (candidates.isEmpty()) {
            return set;
        }
        List<Integer> uniq = new ArrayList<>(new HashSet<>(candidates));
        List<Integer> followed = userFollowDao.findFollowedAmong(viewer, uniq);
        if (followed != null) {
            set.addAll(followed);
        }
        return set;
    }

    /**
     * 解析目标用户ID
     */
    private Integer resolveTargetUid(Integer uidParam, HttpServletRequest request) {
        if (uidParam != null) {
            return uidParam;
        }
        return AppTokenUtil.resolveUid(request);
    }

    /**
     * 显示名称
     */
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
