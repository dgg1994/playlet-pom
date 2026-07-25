package com.playlet.internal.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.TheaterSearchHistoryRespEntity;
import com.playlet.internal.api.response.TheaterWatchHistoryItemEntity;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.Constants;
import com.playlet.internal.constants.TheaterConstants;
import com.playlet.internal.dao.drama.DramaDao;
import com.playlet.internal.dao.drama.UserWatchHistoryDao;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.UserWatchHistoryEntity;
import com.playlet.internal.enums.WelfareActionTypeEnums;
import com.playlet.internal.service.DramaRankStatService;
import com.playlet.internal.service.TheaterService;
import com.playlet.internal.service.WatchGiftService;
import com.playlet.internal.service.WelfareTaskService;
import com.playlet.internal.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.playlet.internal.constants.RedisKeyConstants.*;

@Slf4j
@RestController
@CrossOrigin
public class TheaterServiceImpl extends BaseApiService implements TheaterService {

    @Autowired
    private DramaDao dramaDao;
    @Autowired
    private UserWatchHistoryDao userWatchHistoryDao;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private WelfareTaskService welfareTaskService;
    @Autowired
    private WatchGiftService watchGiftService;
    @Autowired
    private DramaRankStatService dramaRankStatService;

    @Override
    public ResponseBase searchHistory(HttpServletRequest request) {
        TheaterSearchHistoryRespEntity resp = new TheaterSearchHistoryRespEntity();
        String key = historyRedisKey(request);
        if (key == null) {
            return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
        }
        List<Object> raw = redisUtil.lGet(key, 0, HISTORY_MAX - 1);
        if (raw != null) {
            for (Object o : raw) {
                if (o != null && StringUtils.isNotEmpty(o.toString())) {
                    resp.getKeywords().add(o.toString());
                }
            }
        }
        return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase deleteSearchHistory(@RequestParam String keyword, HttpServletRequest request) {
        String key = historyRedisKey(request);
        if (key == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        if (StringUtils.isEmpty(keyword) || StringUtils.isEmpty(keyword.trim())) {
            return setResultError(I18nUtil.getMessage("base_error"));
        }
        redisUtil.lRemove(key, 0, keyword.trim());
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase clearSearchHistory(HttpServletRequest request) {
        String key = historyRedisKey(request);
        if (key == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        redisUtil.del(key);
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase reportWatch(@RequestBody UserWatchHistoryEntity entity, HttpServletRequest request) {
        try {
            Integer uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
            }
            if (entity == null || entity.getDramaId() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            Integer dramaId = entity.getDramaId();
            DramaEntity drama = dramaDao.findByDramaId(dramaId);
            if (drama == null) {
                return setResultError(I18nUtil.getMessage("drama_null"));
            }

            // 添加观看历史
            UserWatchHistoryEntity row = new UserWatchHistoryEntity();
            row.setUid(uid);
            row.setDramaId(dramaId);
            row.setEpisodeId(StringUtils.isEmpty(entity.getEpisodeId()) ? null : entity.getEpisodeId().trim());
            row.setWatchProgress(entity.getWatchProgress() == null ? 0 : Math.max(0, entity.getWatchProgress()));
            GenericityUtil.setDate(row);
            userWatchHistoryDao.upsert(row);
            cacheWatchAfterWrite(uid, dramaId, row);

            // 观看上报
            int deltaSec = normalizeDeltaSeconds(entity.getDeltaSeconds());
            // 单集时长
            int episodeDurationSec = resolveEpisodeDurationSec(entity.getEpisodeProgress());

            pushWelfareWatch(uid, dramaId, row.getEpisodeId());
            if (deltaSec > 0) {
                // 观看礼
                pushWatchGift(uid, dramaId, row, deltaSec);
                // 任务
                pushWelfareWatch(uid, dramaId, row.getEpisodeId());
                // 热度
                incrHotScoreByWatch(dramaId, deltaSec, episodeDurationSec);
            }
            // 每次上报记 1 次 pv；有效秒数用裁剪后的 delta
            pushRankWatchStat(dramaId, deltaSec);

            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 上报有效秒数裁剪：小于 0 的视作 0。
     */
    private int normalizeDeltaSeconds(Integer deltaSeconds) {
        if (deltaSeconds == null || deltaSeconds <= 0) {
            return 0;
        }
        return Math.min(deltaSeconds, TheaterConstants.MAX_DELTA_SEC_PER_REPORT);
    }

    /**
     * 单集时长裁剪：小于 10 秒的视作 10 秒，大于 1 小时的视作 1 小时。
     */
    private int resolveEpisodeDurationSec(Integer clientDurationSec) {
        if (clientDurationSec == null || clientDurationSec <= 0) {
            return TheaterConstants.DEFAULT_EPISODE_DURATION_SEC;
        }
        return Math.min(TheaterConstants.MAX_EPISODE_DURATION_SEC,
                Math.max(TheaterConstants.MIN_EPISODE_DURATION_SEC, clientDurationSec));
    }

    /**
     * 热度：每有效观看「单集时长 / HOT_SCORE_DURATION_DIVISOR」+1。
     * 热度增量 add = floor(本次有效观看秒数 × 3 ÷ 单集总时长)
     * 每看满「单集时长的 1/3」，热度 +1。
     */
    private void incrHotScoreByWatch(Integer dramaId, int deltaSec, int episodeDurationSec) {
        if (dramaId == null || deltaSec <= 0 || episodeDurationSec <= 0) {
            return;
        }
        long add = (long) deltaSec * TheaterConstants.HOT_SCORE_DURATION_DIVISOR / episodeDurationSec;
        if (add <= 0) {
            return;
        }
        try {
            dramaDao.incrHotScore(dramaId, add);
        } catch (Exception e) {
            log.warn("incr hot score failed dramaId={} delta={} duration={}: {}",
                    dramaId, deltaSec, episodeDurationSec, e.getMessage());
        }
    }

    /**
     * 观看福利：每有效观看「单集时长」+1。
     * @param uid 用户 uid
     * @param dramaId dramaId
     * @param episodeId episodeId
     */
    private void pushWelfareWatch(Integer uid, Integer dramaId, String episodeId) {
        if (StringUtils.isEmpty(episodeId)) {
            return;
        }
        try {
            JSONObject ext = new JSONObject();
            ext.put("dramaId", dramaId);
            ext.put("episodeId", episodeId);
            welfareTaskService.onAction(uid, WelfareActionTypeEnums.WATCH, 1, ext.toJSONString());
        } catch (Exception e) {
            log.warn("welfare watch progress failed: {}", e.getMessage());
        }
    }

    /**
     * 观看礼物：每有效观看「单集时长」+1。
     */
    private void pushWatchGift(Integer uid, Integer dramaId, UserWatchHistoryEntity row, int deltaSec) {
        try {
            JSONObject ext = new JSONObject();
            ext.put("dramaId", dramaId);
            ext.put("episodeId", row.getEpisodeId());
            ext.put("watchProgress", row.getWatchProgress());
            watchGiftService.addWatchSeconds(uid, deltaSec, ext.toJSONString());
        } catch (Exception e) {
            log.warn("watch gift seconds failed: {}", e.getMessage());
        }
    }

    /**
     * 观看排行榜：每有效观看「单集时长」+1。
     */
    private void pushRankWatchStat(Integer dramaId, int deltaSec) {
        try {
            dramaRankStatService.onWatch(dramaId, Math.max(0, deltaSec));
        } catch (Exception e) {
            log.warn("rank stat watch failed: {}", e.getMessage());
        }
    }

    @Override
    public ResponseBase watchHistory(UserWatchHistoryEntity entity, HttpServletRequest request) {
        Integer uid = AppTokenUtil.resolveUid(request);
        if (uid == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        if (entity == null) {
            entity = new UserWatchHistoryEntity();
        }
        List<UserWatchHistoryEntity> rows = userWatchHistoryDao.findByUid(uid);
        if (rows == null) {
            rows = new ArrayList<>();
        }
        List<UserWatchHistoryEntity> pageRows = GenericityUtil.Page(rows, entity.getPageNumber(), entity.getPageSize());
        List<TheaterWatchHistoryItemEntity> items = new ArrayList<>();
        for (UserWatchHistoryEntity row : pageRows) {
            TheaterWatchHistoryItemEntity item = toWatchItem(row);
            if (item != null) {
                items.add(item);
            }
        }
        PageInfo<TheaterWatchHistoryItemEntity> page = new PageInfo<>(items);
        page.setTotal(rows.size());
        return setResultSuccess(page, I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase deleteWatchHistory(@RequestParam Integer dramaId, HttpServletRequest request) {
        try {
            Integer uid = AppTokenUtil.resolveUid(request);
            if (uid == null) {
                return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
            }
            if (dramaId == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            userWatchHistoryDao.deleteByUidAndDrama(uid, dramaId);
            // Redis 写入时 dramaId 为 String
            String dramaIdStr = String.valueOf(dramaId);
            redisUtil.lRemove(VIEW_LIST_KEY + uid, 0, dramaIdStr);
            redisUtil.hdel(VIEW_META_KEY + uid, dramaIdStr);
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase clearWatchHistory(HttpServletRequest request) {
        Integer uid = AppTokenUtil.resolveUid(request);
        if (uid == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        userWatchHistoryDao.deleteByUid(uid);
        redisUtil.del(VIEW_LIST_KEY + uid, VIEW_META_KEY + uid, VIEW_EMPTY_KEY + uid);
        return setResultSuccess(I18nUtil.getMessage("base_success"));
    }

    /**
     * 写 MySQL 后同步 Redis：List 保序 + Hash 存进度
     */
    private void cacheWatchAfterWrite(Integer uid, Integer dramaId, UserWatchHistoryEntity row) {
        try {
            redisUtil.del(VIEW_EMPTY_KEY + uid);
            String listKey = VIEW_LIST_KEY + uid;
            String metaKey = VIEW_META_KEY + uid;
            if (!redisUtil.hasKey(listKey)) {
                rebuildViewCache(uid);
                return;
            }
            redisUtil.lRemove(listKey, 0, dramaId);
            redisUtil.lLeftPush(listKey, dramaId);
            redisUtil.lTrim(listKey, 0, VIEW_HISTORY_MAX - 1);
            redisUtil.hset(metaKey, dramaId.toString(), toMetaJson(row));
            redisUtil.expire(listKey, VIEW_HISTORY_TTL_SEC);
            redisUtil.expire(metaKey, VIEW_HISTORY_TTL_SEC);
        } catch (Exception e) {
            log.warn("cacheWatchAfterWrite failed: {}", e.getMessage());
        }
    }

    private void rebuildViewCache(Integer uid) {
        String listKey = VIEW_LIST_KEY + uid;
        String metaKey = VIEW_META_KEY + uid;
        String emptyKey = VIEW_EMPTY_KEY + uid;
        try {
            redisUtil.del(listKey, metaKey, emptyKey);
            List<UserWatchHistoryEntity> rows = userWatchHistoryDao.findByUidLimit(uid, VIEW_HISTORY_MAX);
            if (rows == null || rows.isEmpty()) {
                redisUtil.set(emptyKey, "1", 60);
                return;
            }
            // rows 已按 gmtModified desc；从旧到新 leftPush，最新在队头
            for (int i = rows.size() - 1; i >= 0; i--) {
                UserWatchHistoryEntity row = rows.get(i);
                if (row == null || row.getDramaId() == null) {
                    continue;
                }
                redisUtil.lLeftPush(listKey, row.getDramaId());
                redisUtil.hset(metaKey, row.getDramaId().toString(), toMetaJson(row));
            }
            redisUtil.expire(listKey, VIEW_HISTORY_TTL_SEC);
            redisUtil.expire(metaKey, VIEW_HISTORY_TTL_SEC);
        } catch (Exception e) {
            log.warn("rebuildViewCache failed: {}", e.getMessage());
        }
    }

    private String toMetaJson(UserWatchHistoryEntity row) {
        JSONObject meta = new JSONObject();
        meta.put("episodeId", row.getEpisodeId());
        meta.put("watchProgress", row.getWatchProgress() == null ? 0 : row.getWatchProgress());
        meta.put("gmtModified", row.getGmtModified() == null ? null : row.getGmtModified().getTime());
        return meta.toJSONString();
    }

    private TheaterWatchHistoryItemEntity toWatchItem(UserWatchHistoryEntity row) {
        if (row == null || row.getDramaId() == null) {
            return null;
        }
        DramaEntity drama = dramaDao.findByDramaId(Integer.valueOf(row.getDramaId()));
        if (drama == null) {
            return null;
        }
        TheaterWatchHistoryItemEntity item = new TheaterWatchHistoryItemEntity();
        item.setDramaId(row.getDramaId());
        item.setTitle(drama.getDramaTitle());
        item.setCoverUrl(drama.getCoverUrl());
        item.setTotalEpisodes(drama.getTotalEpisodes());
        item.setFinished(drama.getFinishedState());
        item.setEpisodeId(row.getEpisodeId());
        item.setWatchProgress(row.getWatchProgress() == null ? 0 : row.getWatchProgress());
        item.setGmtModified(row.getGmtModified());
        return item;
    }


    /**
     * 仅登录用户：按 uid 存 Redis
     */
    private String historyRedisKey(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Integer uid = AppTokenUtil.resolveUid(request);
        if (uid != null) {
            return HISTORY_KEY_UID + uid;
        }
        return null;
    }
}
