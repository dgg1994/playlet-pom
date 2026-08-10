package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageInfo;
import com.github.pagehelper.PageHelper;
import com.playlet.oversea.api.response.TheaterSearchHistoryRespEntity;
import com.playlet.oversea.api.response.TheaterWatchHistoryItemEntity;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.drama.DramaDao;
import com.playlet.oversea.dao.drama.UserWatchHistoryDao;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.entity.drama.UserWatchHistoryEntity;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.service.TheaterService;
import com.playlet.oversea.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.playlet.oversea.constants.RedisKeyConstants.*;

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
    private MediaUrlService mediaUrlService;

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
    public ResponseBase deleteSearchHistory(@RequestParam("keyword") String keyword, HttpServletRequest request) {
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
    public ResponseBase watchHistory(UserWatchHistoryEntity entity, HttpServletRequest request) {
        Integer uid = AppTokenUtil.resolveUid(request);
        if (uid == null) {
            return setResultError(Constants.HTTP_RES_CODE_403, I18nUtil.getMessage("login_required"));
        }
        if (entity == null) {
            entity = new UserWatchHistoryEntity();
        }
        // SQL 层分页
        PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
        List<UserWatchHistoryEntity> rows = userWatchHistoryDao.findByUid(uid);
        if (rows == null) {
            rows = new ArrayList<>();
        }
        PageInfo<UserWatchHistoryEntity> basePage = new PageInfo<>(rows);
        Map<Integer, DramaEntity> dramaMap = loadWatchDramaMap(rows);
        List<TheaterWatchHistoryItemEntity> items = new ArrayList<>();
        for (UserWatchHistoryEntity row : rows) {
            TheaterWatchHistoryItemEntity item = toWatchItem(row, dramaMap);
            if (item != null) {
                items.add(item);
            }
        }
        PageInfo<TheaterWatchHistoryItemEntity> page = new PageInfo<>(items);
        page.setTotal(basePage.getTotal());
        page.setPageNum(basePage.getPageNum());
        page.setPageSize(basePage.getPageSize());
        page.setPages(basePage.getPages());
        page.setHasNextPage(basePage.isHasNextPage());
        page.setHasPreviousPage(basePage.isHasPreviousPage());
        return setResultSuccess(page, I18nUtil.getMessage("base_success"));
    }

    @Override
    public ResponseBase deleteWatchHistory(@RequestParam("dramaId") Integer dramaId, HttpServletRequest request) {
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
     * 获取观看历史列表
     *
     * @param row 观看历史
     * @param dramaMap  播放列表
     * @return
     */
    private TheaterWatchHistoryItemEntity toWatchItem(UserWatchHistoryEntity row,
            Map<Integer, DramaEntity> dramaMap) {
        if (row == null || row.getDramaId() == null) {
            return null;
        }
        Integer dramaId;
        try {
            dramaId = Integer.valueOf(row.getDramaId());
        } catch (Exception e) {
            return null;
        }
        DramaEntity drama = dramaMap == null ? null : dramaMap.get(dramaId);
        if (drama == null) {
            return null;
        }
        TheaterWatchHistoryItemEntity item = new TheaterWatchHistoryItemEntity();
        item.setDramaId(row.getDramaId());
        item.setTitle(drama.getDramaTitle());
        item.setCoverUrl(mediaUrlService.sign(drama.getCoverUrl()));
        item.setTotalEpisodes(drama.getTotalEpisodes());
        item.setFinished(drama.getFinishedState());
        item.setEpisodeId(row.getEpisodeId());
        item.setWatchProgress(row.getWatchProgress() == null ? 0 : row.getWatchProgress());
        item.setGmtModified(row.getGmtModified());
        return item;
    }

    /**
     * 获取用户观看记录列表 Redis 缓存 key
     */
    private Map<Integer, DramaEntity> loadWatchDramaMap(List<UserWatchHistoryEntity> rows) {
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Integer> ids = new HashSet<>();
        for (UserWatchHistoryEntity row : rows) {
            if (row == null || row.getDramaId() == null) {
                continue;
            }
            try {
                ids.add(Integer.valueOf(row.getDramaId()));
            } catch (Exception ignored) {
                // 与原先 Integer.valueOf 失败时行为一致：该行后续 toWatchItem 也会失败跳过
            }
        }
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DramaEntity> list = dramaDao.findByIds(new ArrayList<>(ids));
        if (list == null || list.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, DramaEntity> map = new HashMap<>(list.size());
        for (DramaEntity d : list) {
            if (d != null && d.getId() != null) {
                map.put(d.getId(), d);
            }
        }
        return map;
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
