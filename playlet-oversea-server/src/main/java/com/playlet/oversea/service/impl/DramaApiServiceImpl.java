package com.playlet.oversea.service.impl;


import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.oversea.dao.drama.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.api.response.VideoDownloadUrlResp;
import com.playlet.oversea.api.response.VideoPlayUrlResp;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.entity.drama.DramaAssetEntity;
import com.playlet.oversea.entity.drama.DramaEntity;
import com.playlet.oversea.entity.drama.TagEntity;
import com.playlet.oversea.entity.drama.UserDramaCollectEntity;
import com.playlet.oversea.entity.drama.UserDramaLikeEntity;
import com.playlet.oversea.enums.DeleteStateEnum;
import com.playlet.oversea.enums.PublicEnums;
import com.playlet.oversea.enums.VerifyStateEnums;
import com.playlet.oversea.enums.VideoDefinitionEnums;
import com.playlet.oversea.query.drama.BatchVideoDownloadQuery;
import com.playlet.oversea.query.drama.RecommendDramaQuery;
import com.playlet.oversea.api.response.DramaAssetRespEntity;
import com.playlet.oversea.api.response.RecommendDramaRespEntity;
import com.playlet.oversea.api.response.RecommendPageRespEntity;
import com.playlet.oversea.api.response.RecommendVidoeRespEntity;
import com.playlet.oversea.service.DramaApiService;
import com.playlet.oversea.service.MediaUrlService;
import com.playlet.oversea.utils.AppTokenUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.QiniuUploadUtils;
import com.playlet.oversea.utils.StringUtils;

@RestController
@Transactional(rollbackFor = Exception.class)
@CrossOrigin
@Slf4j
public class DramaApiServiceImpl extends BaseApiService implements DramaApiService {

    @Autowired
    private DramaDao dramaDao;

    @Autowired
    private DramaAssetDao dramaAssetDao;

    @Autowired
    private DramaVideoCommentDao dramaVideoCommentDao;

    @Autowired
    private TagDao tagDao;

    @Autowired
    private UserDramaLikeDao userDramaLikeDao;

    @Autowired
    private UserDramaCollectDao userDramaCollectDao;

    @Autowired
    private MediaUrlService mediaUrlService;

    @Override
    public ResponseBase recommend(@RequestBody RecommendDramaQuery entity, HttpServletRequest request) {
        try {
            Integer uid = AppTokenUtil.resolveUid(request);
            // 生成seed
            ensureRecommendSeed(entity);
            PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
            int deleteState = DeleteStateEnum.NORMAL.getIndex();
            entity.setDeleteState(deleteState);
            entity.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
            List<RecommendDramaRespEntity> list = dramaDao.recommendList(entity);
            if (list != null && !list.isEmpty()) {
                // 获取资源id列表
                List<Integer> dramaIds = list.stream().map(RecommendDramaRespEntity::getId).collect(Collectors.toList());
                // 查看资源的第一集
                List<DramaAssetEntity> firstAssets = dramaAssetDao.findFirstAssetsByDramaIds(dramaIds, deleteState);
                Map<Integer, DramaAssetEntity> assetByDramaId = firstAssets == null ? Collections.emptyMap()
                        : firstAssets.stream().filter(a -> a.getDramaId() != null)
                        .collect(Collectors.toMap(DramaAssetEntity::getDramaId, a -> a, (a, b) -> a));

                Set<String> likedEpisodeIds = Collections.emptySet();
                Set<Integer> collectedDramaIds = Collections.emptySet();
                if (uid != null) {
                    // 获取用户的资源id
                    List<Integer> assetIds = assetByDramaId.values().stream()
                            .map(DramaAssetEntity::getId).filter(id -> id != null)
                            .collect(Collectors.toList());
                    if (!assetIds.isEmpty()) {
                        List<String> episodeIds = assetIds.stream().map(String::valueOf).collect(Collectors.toList());
                        // 用户喜欢
                        List<UserDramaLikeEntity> likedRows = userDramaLikeDao.findByUidAndEpisodeIds(uid, episodeIds);
                        likedEpisodeIds = likedRows == null ? Collections.emptySet()
                                : likedRows.stream().map(UserDramaLikeEntity::getEpisodeId).collect(Collectors.toSet());
                        // 按资源 id 查 collect.drama_id（非整剧 id）用户收藏
                        List<UserDramaCollectEntity> collectRows = userDramaCollectDao.findByUidAndDramaIds(uid, assetIds);
                        collectedDramaIds = collectRows == null ? Collections.emptySet()
                                : collectRows.stream().map(UserDramaCollectEntity::getDramaId).collect(Collectors.toSet());
                    }
                }

                for (RecommendDramaRespEntity dramaRes : list) {
                    dramaRes.setCoverUrl(mediaUrlService.sign(dramaRes.getCoverUrl()));
                    DramaAssetEntity asset = assetByDramaId.get(dramaRes.getId());
                    if (asset == null) {
                        continue;
                    }
                    // 转换为推荐视频
                    RecommendVidoeRespEntity vidoeRes = toRecommendVideoRes(asset);
                    vidoeRes.setCollectScore(dramaRes.getCollectScore());
                    vidoeRes.setShareScore(dramaRes.getShareScore());
                    vidoeRes.setVideoUrl(null);
                    if (uid != null) {
                        if (likedEpisodeIds.contains(String.valueOf(asset.getId()))) {
                            vidoeRes.setIsLike(PublicEnums.ONE.getIndex());
                        }
                        if (collectedDramaIds.contains(asset.getId())) {
                            vidoeRes.setIsCollect(PublicEnums.ONE.getIndex());
                        }
                    } else {
                        vidoeRes.setIsLike(PublicEnums.ZERO.getIndex());
                        vidoeRes.setIsCollect(PublicEnums.ZERO.getIndex());
                    }
                    dramaRes.setVidoeRes(vidoeRes);
                }
            }
            RecommendPageRespEntity resp = new RecommendPageRespEntity();
            resp.setSeed(entity.getSeed());
            resp.setPage(new PageInfo<>(list));
            return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为推荐视频
     * @param asset 资源
     * @return
     */
    private static RecommendVidoeRespEntity toRecommendVideoRes(DramaAssetEntity asset) {
        RecommendVidoeRespEntity res = new RecommendVidoeRespEntity();
        res.setId(asset.getId());
        res.setVideoName(asset.getVideoName());
        res.setSetNum(asset.getSetNum());
        res.setCollectScore(asset.getCollectScore());
        res.setShareScore(asset.getShareScore());
        res.setLikeScore(asset.getLikeScore());
        res.setDiscussScore(asset.getDiscussScore());
        res.setVideoType(asset.getVideoType());
        res.setVideoUrl(asset.getVideoUrl());
        return res;
    }

    /** 首页未传 seed 时生成；翻页需原样回传以保证排序稳定 */
    private void ensureRecommendSeed(RecommendDramaQuery entity) {
        String seed = entity.getSeed() == null ? null : entity.getSeed().trim();
        if (StringUtils.isEmpty(seed)) {
            entity.setSeed(UUID.randomUUID().toString().replace("-", ""));
            return;
        }
        if (seed.length() > 64) {
            seed = seed.substring(0, 64);
        }
        entity.setSeed(seed);
    }


    @Override
    public ResponseBase playVideoReport(Integer id) {
        try {
            DramaEntity dramaEntity = dramaDao.findByVideoId(id);
            if (dramaEntity != null) {
                dramaEntity.setHotScore(dramaEntity.getHotScore() + 1);
                dramaDao.updateById(dramaEntity);
            }
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase selections(Integer id, HttpServletRequest request) {
        try {
            Integer uid = AppTokenUtil.resolveUid(request);
            List<DramaAssetRespEntity> list = dramaAssetDao.findByDramaId(id);
            if (list == null || list.isEmpty()) {
                return setResultSuccess(list, I18nUtil.getMessage("base_success"));
            }
            if (uid == null) {
                for (DramaAssetRespEntity item : list) {
                    item.setIsLike(PublicEnums.ZERO.getIndex());
                    item.setIsCollect(PublicEnums.ZERO.getIndex());
                }
                return setResultSuccess(list, I18nUtil.getMessage("base_success"));
            }
            // 收藏是整剧维度，只查一次
            Integer isCollect = userDramaCollectDao.findByUidAndDrama(uid, id) != null
                    ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex();
            // 点赞按集批量查，避免 N+1
            List<String> episodeIds = list.stream()
                    .map(item -> String.valueOf(item.getId()))
                    .collect(Collectors.toList());
            List<UserDramaLikeEntity> likedRows = userDramaLikeDao.findByUidAndEpisodeIds(uid, episodeIds);
            Set<String> likedEpisodeIds = likedRows == null ? Collections.emptySet()
                    : likedRows.stream()
                    .map(UserDramaLikeEntity::getEpisodeId)
                    .collect(Collectors.toSet());
            for (DramaAssetRespEntity item : list) {
                item.setIsLike(likedEpisodeIds.contains(String.valueOf(item.getId()))
                        ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
                item.setIsCollect(isCollect);
            }
            return setResultSuccess(list, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase getVideoUrl(Integer id) {
        try {
            if (id == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            String keyOrUrl = dramaAssetDao.findVideoUrl(id);
            if (StringUtils.isEmpty(keyOrUrl)) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            return setResultSuccess(buildMultiRatePlayUrl(id, keyOrUrl), I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase getVideoDownloadUrl(@Valid @RequestBody BatchVideoDownloadQuery query) {
        try {
            if (query == null || query.getIds() == null || query.getIds().isEmpty()
                    || StringUtils.isEmpty(query.getDefinition())) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            VideoDefinitionEnums definition = VideoDefinitionEnums.ofCode(query.getDefinition().trim());
            if (definition == null) {
                return setResultError(I18nUtil.getMessage("video_definition_invalid"));
            }
            // 去重并保持入参顺序
            List<Integer> ids = new ArrayList<>();
            for (Integer id : query.getIds()) {
                if (id != null && !ids.contains(id)) {
                    ids.add(id);
                }
            }
            if (ids.isEmpty()) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            List<DramaAssetEntity> rows = dramaAssetDao.findIdAndVideoUrlByIds(ids);
            Map<Integer, String> urlMap = new HashMap<>();
            if (rows != null) {
                for (DramaAssetEntity row : rows) {
                    if (row != null && row.getId() != null) {
                        urlMap.put(row.getId(), row.getVideoUrl());
                    }
                }
            }
            List<VideoDownloadUrlResp> result = new ArrayList<>();
            for (Integer id : ids) {
                String keyOrUrl = urlMap.get(id);
                if (StringUtils.isEmpty(keyOrUrl)) {
                    continue;
                }
                VideoDownloadUrlResp item = buildDownloadUrlByDefinition(id, keyOrUrl, definition);
                if (item != null) {
                    result.add(item);
                }
            }
            if (result.isEmpty()) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            return setResultSuccess(result, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 方式A：按命名规则推导候选码率，再用七牛 stat 过滤真实存在的对象后签名返回。
     * 支持：
     * 1) ..._720.m3u8 -> 候选 ..._360/480/720/1080.m3u8
     * 2) oceans.m3u8（旧数据）-> 候选 oceans_360/480/720/1080.m3u8
     * 若多码率都不存在，退回原始 key（存在则单路返回）。
     */
    private VideoPlayUrlResp buildMultiRatePlayUrl(Integer assetId, String keyOrUrl) {
        VideoPlayUrlResp resp = new VideoPlayUrlResp();
        resp.setAssetId(assetId);

        String key = QiniuUploadUtils.extractKey(keyOrUrl);
        if (StringUtils.isEmpty(key)) {
            key = keyOrUrl == null ? "" : keyOrUrl.trim();
        }

        List<VideoPlayUrlResp.StreamItem> streams = new ArrayList<>();
        Matcher multiMatcher = VideoDefinitionEnums.MULTI_RATE_M3U8_PATTERN.matcher(key);
        Matcher plainMatcher = VideoDefinitionEnums.PLAIN_M3U8_PATTERN.matcher(key);
        VideoDefinitionEnums preferred = VideoDefinitionEnums.DEFAULT;
        if (multiMatcher.matches()) {
            VideoDefinitionEnums matched = VideoDefinitionEnums.ofCode(multiMatcher.group(2));
            if (matched != null) {
                preferred = matched;
            }
            fillExistingMultiRateStreams(streams, multiMatcher.group(1));
        } else if (plainMatcher.matches()) {
            fillExistingMultiRateStreams(streams, plainMatcher.group(1));
        }

        if (streams.isEmpty() && QiniuUploadUtils.exists(key)) {
            streams.add(buildStreamItem(VideoDefinitionEnums.DEFAULT, key));
            preferred = VideoDefinitionEnums.DEFAULT;
        }

        resp.setStreams(streams);
        resp.setDefaultDefinition(resolveDefaultDefinition(streams, preferred).getCode());
        return resp;
    }

    /**
     * 按指定清晰度取 MP4：{prefix}_{definition}.mp4；不存在则返回 null。
     */
    private VideoDownloadUrlResp buildDownloadUrlByDefinition(Integer assetId, String keyOrUrl,
            VideoDefinitionEnums definition) {
        String key = QiniuUploadUtils.extractKey(keyOrUrl);
        if (StringUtils.isEmpty(key)) {
            key = keyOrUrl == null ? "" : keyOrUrl.trim();
        }
        String prefix = VideoDefinitionEnums.resolvePrefix(key);
        if (StringUtils.isEmpty(prefix)) {
            return null;
        }
        String mp4Key = definition.toMp4Key(prefix);
        if (!QiniuUploadUtils.exists(mp4Key)) {
            return null;
        }
        VideoDownloadUrlResp resp = new VideoDownloadUrlResp();
        resp.setAssetId(assetId);
        resp.setDefinition(definition.getCode());
        resp.setLabel(definition.getLabel());
        resp.setPath(mp4Key);
        resp.setDownloadUrl(mediaUrlService.signVideo(mp4Key));
        return resp;
    }

    /**
     * 填充已存在的多码率流。
     */
    private void fillExistingMultiRateStreams(List<VideoPlayUrlResp.StreamItem> streams, String prefix) {
        for (VideoDefinitionEnums def : VideoDefinitionEnums.values()) {
            String streamKey = def.toM3u8Key(prefix);
            if (QiniuUploadUtils.exists(streamKey)) {
                streams.add(buildStreamItem(def, streamKey));
            }
        }
    }

    /**
     * 确定默认清晰度。
     */
    private VideoDefinitionEnums resolveDefaultDefinition(List<VideoPlayUrlResp.StreamItem> streams,
                                                          VideoDefinitionEnums preferred) {
        if (streams == null || streams.isEmpty()) {
            return VideoDefinitionEnums.DEFAULT;
        }
        if (containsDefinition(streams, preferred)) {
            return preferred;
        }
        if (containsDefinition(streams, VideoDefinitionEnums.DEFAULT)) {
            return VideoDefinitionEnums.DEFAULT;
        }
        for (VideoDefinitionEnums def : VideoDefinitionEnums.FALLBACK_ORDER) {
            if (containsDefinition(streams, def)) {
                return def;
            }
        }
        VideoDefinitionEnums first = VideoDefinitionEnums.ofCode(streams.get(0).getDefinition());
        return first != null ? first : VideoDefinitionEnums.DEFAULT;
    }

    /**
     * 确定是否存在指定清晰度的流。
     */
    private boolean containsDefinition(List<VideoPlayUrlResp.StreamItem> streams, VideoDefinitionEnums def) {
        if (def == null) {
            return false;
        }
        for (VideoPlayUrlResp.StreamItem item : streams) {
            if (def.getCode().equals(item.getDefinition())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建流项。
     */
    private VideoPlayUrlResp.StreamItem buildStreamItem(VideoDefinitionEnums definition, String streamKey) {
        VideoPlayUrlResp.StreamItem item = new VideoPlayUrlResp.StreamItem();
        item.setDefinition(definition.getCode());
        item.setLabel(definition.getLabel());
        item.setVideoUrl(mediaUrlService.signVideo(streamKey));
        return item;
    }

    @Override
    public ResponseBase workInfo(Integer id) {
        try {
            String language = LanguageContext.getLanguage();
            DramaEntity entity = dramaDao.selectById(id);
            if (entity == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            if (VerifyStateEnums.REMOVED_SHELVES.getIndex().equals(entity.getVerifyStatus())) {
                return setResultError(I18nUtil.getMessage("video_removed_shelves"));
            }
            if (DeleteStateEnum.DELETE.getIndex().equals(entity.getDeleteState())) {
                return setResultError(I18nUtil.getMessage("video_delete"));
            }
            List<TagEntity> tagList = tagDao.findGroupLang(language, entity.getId());
            Double scoreNum = dramaVideoCommentDao.avgScoreNumByDramaId(entity.getId(), DeleteStateEnum.NORMAL.getIndex());
            entity.setScoreNum(scoreNum == null ? 0 : scoreNum);
            entity.setTagList(tagList);
            entity.setCoverUrl(mediaUrlService.sign(entity.getCoverUrl()));
            return setResultSuccess(entity, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase relatedWork(Integer id) {
        try {
            List<RecommendDramaRespEntity> list = dramaDao.relatedWork(id, DeleteStateEnum.NORMAL.getIndex(), VerifyStateEnums.AVAILABLE_NOW.getIndex());
            if (list != null) {
                for (RecommendDramaRespEntity item : list) {
                    item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
                }
            }
            return setResultSuccess(list, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseBase playVideo(Integer id) {
        try {
            RecommendDramaRespEntity dramaRes = dramaDao.findById(id);
            if (dramaRes != null) {
                dramaRes.setCoverUrl(mediaUrlService.sign(dramaRes.getCoverUrl()));
                RecommendVidoeRespEntity vidoeRes = dramaAssetDao.findDramaIdOne(dramaRes.getId(), DeleteStateEnum.NORMAL.getIndex());
                vidoeRes.setCollectScore(dramaRes.getCollectScore());
                vidoeRes.setShareScore(dramaRes.getShareScore());
                if (vidoeRes.getVideoUrl() != null) {
                    vidoeRes.setVideoUrl(mediaUrlService.signVideo(vidoeRes.getVideoUrl()));
                }
                dramaRes.setVidoeRes(vidoeRes);
            }
            return setResultSuccess(dramaRes, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }


    @Override
    public ResponseBase getVideoInfo(Integer id, HttpServletRequest request) {
        try {
            DramaAssetEntity vidoeRes = dramaAssetDao.selectById(id);
            if (vidoeRes != null) {
                DramaEntity dramaEntity = dramaDao.findByVideoId(id);
                if (dramaEntity != null) {
                    vidoeRes.setCollectScore(dramaEntity.getCollectScore());
                    vidoeRes.setShareScore(dramaEntity.getShareScore());
                    vidoeRes.setVideoUrl(null);
                    Integer uid = AppTokenUtil.resolveUid(request);
                    if (uid != null) {
                        UserDramaLikeEntity userDramaLikeEntity = userDramaLikeDao.selectOne(new QueryWrapper<UserDramaLikeEntity>()
                                .eq("uid", uid)
                                .eq("drama_id", dramaEntity.getId())
                                .eq("episode_id", id));
                        //是否点赞
                        vidoeRes.setIsLike(userDramaLikeEntity == null ? PublicEnums.ZERO.getIndex() : PublicEnums.ONE.getIndex());
                        //是否收藏
                        UserDramaCollectEntity userDramaCollectEntity = userDramaCollectDao.selectOne(new QueryWrapper<UserDramaCollectEntity>()
                                .eq("uid", uid)
                                .eq("drama_id", dramaEntity.getId()));
                        vidoeRes.setIsCollect(userDramaCollectEntity == null ? PublicEnums.ZERO.getIndex() : PublicEnums.ONE.getIndex());
                    } else {
                        vidoeRes.setIsLike(PublicEnums.ZERO.getIndex());
                        vidoeRes.setIsCollect(PublicEnums.ZERO.getIndex());
                    }
                }
            }
            return setResultSuccess(vidoeRes, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            log.error("service error", e);
            throw new RuntimeException(e);
        }
    }

}
