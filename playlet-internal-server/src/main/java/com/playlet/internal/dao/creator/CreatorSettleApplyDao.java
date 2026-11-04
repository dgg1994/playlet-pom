package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.CreatorSettleApplyItemEntity;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import com.playlet.internal.query.creator.CreatorSettleApplyQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 管理端入驻申请审核。
 */
@Repository
public interface CreatorSettleApplyDao extends BaseMapper<CreatorProfileEntity> {

    /**
     * 入驻申请列表（creator_profile 关联 creator_account 取联系方式）
     */
    @Select("<script>"
            + "select p.id as applyId, "
            + "  concat('RZ', date_format(ifnull(p.setTime, now()), '%Y'), lpad(p.id, 3, '0')) as applyNo, "
            + "  ifnull(nullif(p.org_name, ''), ifnull(nullif(p.real_name, ''), a.nickname)) as applyName, "
            + "  ifnull(p.identity_type, 1) as identityType, "
            + "  ifnull(p.creator_role, 3) as creatorRole, "
            + "  ifnull(nullif(p.real_name, ''), a.nickname) as contactName, "
            + "  concat(ifnull(a.mobile_prefix, ''), ifnull(a.mobile_number, '')) as contactPhone, "
            + "  p.setTime as applyTime, "
            + "  ifnull(p.audit_status, 0) as auditStatus "
            + "from creator_profile p "
            + "left join creator_account a on a.id = p.creator_id "
            + "where 1=1 "
            + "<if test='q.keyword != null and q.keyword != \"\"'>"
            + "  and (lower(ifnull(p.org_name, '')) like concat('%', lower(#{q.keyword}), '%')"
            + "    or lower(ifnull(p.real_name, '')) like concat('%', lower(#{q.keyword}), '%')"
            + "    or lower(ifnull(a.nickname, '')) like concat('%', lower(#{q.keyword}), '%')"
            + "    or ifnull(a.mobile_number, '') like concat('%', #{q.keyword}, '%'))"
            + "</if>"
            + "<if test='q.identityType != null'>"
            + "  and ifnull(p.identity_type, 1) = #{q.identityType} "
            + "</if>"
            + "<if test='q.auditStatus != null'>"
            + "  and ifnull(p.audit_status, 0) = #{q.auditStatus} "
            + "</if>"
            + "order by p.setTime desc, p.id desc"
            + "</script>")
    List<CreatorSettleApplyItemEntity> findAdminApplyList(@Param("q") CreatorSettleApplyQuery query);

    /**
     * 打开详情时把「待查看(0)」置为「已查看(1)」
     */
    @Update("update creator_profile set audit_status = 1, gmtModified = #{now} "
            + "where id = #{id} and ifnull(audit_status, 0) = 0")
    int markViewed(@Param("id") Integer id, @Param("now") Date now);

    /**
     * 审核通过
     */
    @Update("update creator_profile set audit_status = 2, audit_reject_reason = null, "
            + "audit_pass_time = #{now}, auditor_id = #{adminId}, gmtModified = #{now} where id = #{id}")
    int markApproved(@Param("id") Integer id, @Param("adminId") Integer adminId, @Param("now") Date now);

    /**
     * 审核驳回
     */
    @Update("update creator_profile set audit_status = 3, audit_reject_reason = #{rejectReason}, "
            + "auditor_id = #{adminId}, gmtModified = #{now} where id = #{id}")
    int markRejected(@Param("id") Integer id, @Param("adminId") Integer adminId,
                     @Param("rejectReason") String rejectReason, @Param("now") Date now);
}
