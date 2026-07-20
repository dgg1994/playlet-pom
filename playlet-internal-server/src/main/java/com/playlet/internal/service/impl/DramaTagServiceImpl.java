package com.playlet.internal.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.TagRequest;
import com.playlet.internal.api.response.TagGroupRespEntity;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.dao.drama.TagDao;
import com.playlet.internal.entity.drama.TagEntity;
import com.playlet.internal.service.DramaTagService;
import com.playlet.internal.utils.GenericityUtil;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 类描述：标签管理实现
 *
 * @author GeminiSun
 * @date 2026/07/16 09:33
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional
public class DramaTagServiceImpl implements DramaTagService {

    @Autowired
    private TagDao tagDao;

    @Override
    @SysLogAnnotation(module = "短剧标签", type = "POST", remark = "标签列表")
    public ResponseBase findList(@RequestBody TagEntity entity) {
        if (entity == null) {
            entity = new TagEntity();
        }
        // 按 groupId 分页；多语言字段平铺为 zh-cn / en 等
        PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
        List<TagEntity> groupRows = tagDao.findAdminGroupList(entity);
        PageInfo<TagEntity> pageInfo = new PageInfo<>(groupRows);

        Map<String, List<TagEntity>> tagsByGroup = new LinkedHashMap<>();
        if (!groupRows.isEmpty()) {
            //获取groupId的集合
            List<String> groupIds = groupRows.stream()
                    .map(TagEntity::getGroupId)
                    .filter(gid -> !StringUtils.isEmpty(gid))
                    .collect(Collectors.toList());
            if (!groupIds.isEmpty()) {
                List<TagEntity> allTags = tagDao.findByGroupIds(groupIds);
                if (allTags != null) {
                    for (TagEntity tag : allTags) {
                        tagsByGroup.computeIfAbsent(tag.getGroupId(), k -> new ArrayList<>()).add(tag);
                    }
                }
            }
        }

        List<TagGroupRespEntity> rows = new ArrayList<>();
        for (TagEntity group : groupRows) {
            TagGroupRespEntity row = new TagGroupRespEntity();
            row.setGroupId(group.getGroupId());
            row.setSortWeight(group.getSortWeight());
            row.setStatus(group.getStatus());
            row.setSetTime(group.getSetTime());
            row.setGmtModified(group.getGmtModified());
            List<TagEntity> tags = tagsByGroup.get(group.getGroupId());
            if (tags != null) {
                for (TagEntity tag : tags) {
                    if (!StringUtils.isEmpty(tag.getLangue())) {
                        row.putLangName(tag.getLangue(), tag.getTagName());
                    }
                }
            }
            rows.add(row);
        }
        PageInfo<TagGroupRespEntity> result = new PageInfo<>(rows);
        return setResultSuccess(result, I18nUtil.getMessage("base_success"));
    }

    @Override
    @SysLogAnnotation(module = "短剧标签", type = "POST", remark = "新增标签")
    public ResponseBase save(@RequestBody TagRequest entity) {
        try {
            if (entity == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            //生成groupId
            String groupId = IdUtil.simpleUUID();
            Integer sortWeight = entity.getSortWeight();
            List<TagEntity> tags = entity.getTags();
            for (TagEntity tag : tags) {
                if (tagDao.findByTagName(tag.getTagName().trim()) != null) {
                    return setResultError(I18nUtil.getMessage("base_info_exist"));
                }
                TagEntity tagEntity = new TagEntity();
                tagEntity.setLangue(tag.getLangue());
                tagEntity.setGroupId(groupId);
                tagEntity.setTagName(tag.getTagName().trim());
                tagEntity.setStatus(1);
                tagEntity.setSortWeight(sortWeight);
                GenericityUtil.setDate(tagEntity);
                tagDao.insert(tagEntity);
            }
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SysLogAnnotation(module = "短剧标签", type = "POST", remark = "变更标签状态")
    public ResponseBase changeStatus(@RequestBody TagEntity entity) {
        try {
            if (entity == null || entity.getGroupId() == null || entity.getStatus() == null) {
                return setResultError(I18nUtil.getMessage("base_error"));
            }
            if (entity.getStatus() != 0 && entity.getStatus() != 1) {
                return setResultError("状态仅支持 0停用 / 1启用");
            }
            List<TagEntity> tagEntities = tagDao.selectList(new QueryWrapper<TagEntity>().eq("group_id", entity.getGroupId()));
            if (tagEntities == null) {
                return setResultError(I18nUtil.getMessage("base_data_null"));
            }
            tagDao.updateStatusByGroupId(entity.getStatus(),entity.getGroupId());
            return setResultSuccess(I18nUtil.getMessage("base_success"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}