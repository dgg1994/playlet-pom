package com.playlet.internal.dao.settle;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.settle.SettleManageEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettleManageDao extends BaseMapper<SettleManageEntity> {

    /**
     * 管理端分页列表（keyword 模糊搜 settle_no / partner / drama_name；status 精确过滤）
     */
    @Select("<script>"
            + "select * from settle_manage where 1=1 "
            + "<if test='status != null'> and status = #{status} </if>"
            + "<if test='keyword != null and keyword != \"\"'>"
            + "  and (settle_no like concat('%',#{keyword},'%')"
            + "       or partner like concat('%',#{keyword},'%')"
            + "       or drama_name like concat('%',#{keyword},'%'))"
            + "</if>"
            + " order by id desc"
            + "</script>")
    List<SettleManageEntity> findAdminList(SettleManageEntity entity);

    /**
     * 批量置为已结算（status=1, settle_time=NOW()），仅更新待结算的
     * @param ids 结算单主键列表
     * @return 实际更新行数
     */
    @Update("<script>"
            + "update settle_manage set status = 1, settle_time = NOW() "
            + "where status = 0 and id in "
            + "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    int markSettledByIds(@Param("ids") List<Long> ids);

    /** 按 settle_no 查（用于生成单号前校验唯一） */
    @Select("select id from settle_manage where settle_no = #{settleNo} limit 1")
    Long findIdBySettleNo(@Param("settleNo") String settleNo);
}
