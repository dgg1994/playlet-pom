package com.playlet.internal.service;

import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.entity.banner.BannerManageEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 管理端 Banner 与开屏：网关 /china/admin/bannerManage/**
 */
@RequestMapping("/bannerManage")
@Api(value = "Banner与开屏管理", tags = "Banner与开屏管理")
public interface BannerManageService {

    @PostMapping("/findList")
    @ApiOperation(value = "分页列表", notes = "keyword 模糊搜 title / previewText；status 由后端按时间计算")
    ResponseBase findList(BannerManageEntity entity);

    @PostMapping("/save")
    @ApiOperation(value = "新增 Banner")
    ResponseBase save(BannerManageEntity entity);

    @PostMapping("/update")
    @ApiOperation(value = "编辑 Banner")
    ResponseBase update(BannerManageEntity entity);

    @PostMapping("/delete")
    @ApiOperation(value = "删除 Banner")
    ResponseBase delete(BannerManageEntity entity);
}
