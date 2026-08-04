package com.playlet.internal.api.request;

import com.playlet.internal.entity.drama.TagEntity;
import lombok.Data;

import java.util.List;

/** 标签新增请求 */
@Data
public class TagRequest {

    private List<TagEntity> tags;

    private Integer sortWeight;

    private String groupId;

}