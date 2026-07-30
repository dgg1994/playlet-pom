package com.playlet.internal.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.playlet.internal.dao.drama.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.api.response.VideoPlayUrlResp;
import com.playlet.internal.base.BaseApiService;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.config.heard.LanguageContext;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.entity.drama.UserDramaCollectEntity;
import com.playlet.internal.entity.drama.UserDramaLikeEntity;
import com.playlet.internal.enums.DeleteStateEnum;
import com.playlet.internal.enums.PublicEnums;
import com.playlet.internal.enums.VerifyStateEnums;
import com.playlet.internal.enums.VideoDefinitionEnums;
import com.playlet.internal.query.drama.RecommendDramaQuery;
import com.playlet.internal.response.drama.DramaAssetRes;
import com.playlet.internal.response.drama.RecommendDramaRes;
import com.playlet.internal.response.drama.RecommendPageResp;
import com.playlet.internal.response.drama.RecommendVidoeRes;
import com.playlet.internal.service.DramaApiService;
import com.playlet.internal.service.MediaUrlService;
import com.playlet.internal.utils.AppTokenUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.QiniuUploadUtils;
import com.playlet.internal.utils.StringUtils;

@RestController
@Transactional
@CrossOrigin
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
            ensureRecommendSeed(entity);
            PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
            entity.setDeleteState(DeleteStateEnum.NORMAL.getIndex());
            entity.setVerifyStatus(VerifyStateEnums.AVAILABLE_NOW.getIndex());
            List<RecommendDramaRes> list = dramaDao.recommendList(entity);
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).setCoverUrl(mediaUrlService.sign(list.get(i).getCoverUrl()));
                    RecommendVidoeRes vidoeRes = dramaAssetDao.findDramaIdOne(list.get(i).getId(), DeleteStateEnum.NORMAL.getIndex());
                    vidoeRes.setCollectScore(list.get(i).getCollectScore());
                    vidoeRes.setShareScore(list.get(i).getShareScore());
                    vidoeRes.setVideoUrl(null);
                    if (uid != null) {
                        UserDramaLikeEntity dramaLikeEntity = userDramaLikeDao.findByVoideId(vidoeRes.getId(),uid);
                        if (dramaLikeEntity != null) {
                            vidoeRes.setIsLike(PublicEnums.ONE.getIndex());
                        }
                        UserDramaCollectEntity collectEntity = userDramaCollectDao.findByVoideId(vidoeRes.getId(),uid);
                        if (collectEntity != null) {
                            vidoeRes.setIsCollect(PublicEnums.ONE.getIndex());
                        }
                    } else {
                        vidoeRes.setIsLike(PublicEnums.ZERO.getIndex());
                        vidoeRes.setIsCollect(PublicEnums.ZERO.getIndex());
                    }
                    list.get(i).setVidoeRes(vidoeRes);
                }
            }
            RecommendPageResp resp = new RecommendPageResp();
            resp.setSeed(entity.getSeed());
            resp.setPage(new PageInfo<>(list));
            return setResultSuccess(resp, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
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
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public ResponseBase selections(Integer id, HttpServletRequest request) {
        try {
            Integer uid = AppTokenUtil.resolveUid(request);
            List<DramaAssetRes> list = dramaAssetDao.findByDramaId(id);
            if (list == null || list.isEmpty()) {
                return setResultSuccess(list, I18nUtil.getMessage("base_success"));
            }
            if (uid == null) {
                for (DramaAssetRes item : list) {
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
            for (DramaAssetRes item : list) {
                item.setIsLike(likedEpisodeIds.contains(String.valueOf(item.getId()))
                        ? PublicEnums.ONE.getIndex() : PublicEnums.ZERO.getIndex());
                item.setIsCollect(isCollect);
            }
            return setResultSuccess(list, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
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
            e.printStackTrace();
            throw new RuntimeException();
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

    private void fillExistingMultiRateStreams(List<VideoPlayUrlResp.StreamItem> streams, String prefix) {
        for (VideoDefinitionEnums def : VideoDefinitionEnums.values()) {
            String streamKey = def.toM3u8Key(prefix);
            if (QiniuUploadUtils.exists(streamKey)) {
                streams.add(buildStreamItem(def, streamKey));
            }
        }
    }

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
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public ResponseBase relatedWork(Integer id) {
        try {
            List<RecommendDramaRes> list = dramaDao.relatedWork(id, DeleteStateEnum.NORMAL.getIndex(), VerifyStateEnums.AVAILABLE_NOW.getIndex());
            if (list != null) {
                for (RecommendDramaRes item : list) {
                    item.setCoverUrl(mediaUrlService.sign(item.getCoverUrl()));
                }
            }
            return setResultSuccess(list, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    public ResponseBase playVideo(Integer id) {
        try {
            RecommendDramaRes dramaRes = dramaDao.findById(id);
            if (dramaRes != null) {
                dramaRes.setCoverUrl(mediaUrlService.sign(dramaRes.getCoverUrl()));
                RecommendVidoeRes vidoeRes = dramaAssetDao.findDramaIdOne(dramaRes.getId(), DeleteStateEnum.NORMAL.getIndex());
                vidoeRes.setCollectScore(dramaRes.getCollectScore());
                vidoeRes.setShareScore(dramaRes.getShareScore());
                if (vidoeRes.getVideoUrl() != null) {
                    vidoeRes.setVideoUrl(mediaUrlService.signVideo(vidoeRes.getVideoUrl()));
                }
                dramaRes.setVidoeRes(vidoeRes);
            }
            return setResultSuccess(dramaRes, I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
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
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

}
