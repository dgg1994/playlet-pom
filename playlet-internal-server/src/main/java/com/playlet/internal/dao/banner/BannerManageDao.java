package com.playlet.internal.dao.banner;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.banner.BannerManageEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerManageDao extends BaseMapper<BannerManageEntity> {

    /**
     * 管理端分页列表
     * keyword 模糊搜 title / preview_text（忽略大小写）
     * 按 sort_weight DESC, id DESC
     */
    @Select("<script>"
            + "select * from banner_manage where 1=1 "
            + "<if test='keyword != null and keyword != \"\"'>"
            + "  and (lower(title) like concat('%',lower(#{keyword}),'%')"
            + "       or lower(preview_text) like concat('%',lower(#{keyword}),'%'))"
            + "</if>"
            + " order by sort_weight desc, id desc"
            + "</script>")
    List<BannerManageEntity> findAdminList(BannerManageEntity entity);
}
