package com.playlet.oversea.api.request;

import com.playlet.oversea.entity.drama.TagEntity;
import lombok.Data;

import java.util.List;

/** 标签新增请求 */
@Data
public class TagRequest {

    private List<TagEntity> tags;

    private Integer sortWeight;

    private String groupId;

}