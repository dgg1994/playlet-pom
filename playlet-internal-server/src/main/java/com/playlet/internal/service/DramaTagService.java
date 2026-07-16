package com.playlet.internal.service;

import com.playlet.internal.api.request.TagRequest;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.drama.TagEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 类描述：B端-标签管理
 *
 * @author GeminiSun
 * @date 2026/07/16 09:32
 */
@RequestMapping("/dramaTag")
@Api(value = "短剧标签", tags = "短剧标签")
public interface DramaTagService {

    @PostMapping("/findList")
    @ApiOperation("标签分页列表")
    ResponseBase findList(TagEntity entity);

    @PostMapping("/save")
    @ApiOperation("新增标签")
    ResponseBase save(TagRequest tagRequest);


    @PostMapping("/changeStatus")
    @ApiOperation("启用/停用标签")
    ResponseBase changeStatus(TagEntity entity);

}
