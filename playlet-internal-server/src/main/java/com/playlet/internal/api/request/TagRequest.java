package com.playlet.internal.api.request;

import com.playlet.internal.entity.drama.TagEntity;
import lombok.Data;

import java.util.List;

/**
 * 类描述：标签新增请求
 *
 * @author GeminiSun
 * @date 2026/07/16 13:55
 */
@Data
public class TagRequest {

    private List<TagEntity> tags;

    private Integer sortWeight;

}