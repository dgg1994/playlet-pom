package com.playlet.internal.dao.drama;

import com.playlet.internal.query.drama.DramaWorkAuditQuery;
import com.playlet.internal.api.response.DramaWorkAuditListRespEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作品评审列表查询（剧/集，按 auditStatus 筛选）。
 */
@Repository
public interface DramaWorkAuditDao {

	/**
	 * 作品管理列表（仅剧）。
	 * auditStatus：0/1 待审核（含待审+审核中），2 通过，3 驳回，4 申诉中。
	 */
	@Select("<script>"
			+ "select d.id as bizId, d.id as dramaId, cast(null as signed) as assetId, 1 as workType,"
			+ "  d.drama_title as workName, d.cover_url as coverUrl,"
			+ "  d.description_info as descriptionInfo, d.producer_firm as producerFirm,"
			+ "  d.audit_status as auditStatus, ifnull(d.shelf_status, 0) as shelfStatus,"
			+ "  d.audit_reject_reason as auditRejectReason,"
			+ "  ifnull(d.appeal_status, 0) as appealStatus, d.appeal_reason as appealReason, d.appeal_time as appealTime,"
			+ "  ifnull(ai.status, 0) as aiStatus, ai.handle_remark as aiHandleRemark,"
			+ "  ifnull(ga.status, 0) as groupAStatus, ga.handle_remark as groupAHandleRemark,"
			+ "  ifnull(gb.status, 0) as groupBStatus, gb.handle_remark as groupBHandleRemark,"
			+ "  d.setTime as uploadDate"
			+ " from drama d"
			+ " left join drama_audit_step ai on ai.drama_id = d.id and ai.step_type = 1"
			+ " left join drama_audit_step ga on ga.drama_id = d.id and ga.step_type = 2"
			+ " left join drama_audit_step gb on gb.drama_id = d.id and gb.step_type = 3"
			+ " where ifnull(d.delete_state, 0) = 0"
			+ " <if test='q.dramaId != null'> and d.id = #{q.dramaId} </if>"
			+ " <if test='q.keyword != null and q.keyword != \"\"'> and d.drama_title like concat('%', #{q.keyword}, '%') </if>"
			+ " <if test='q.auditStatus != null and (q.auditStatus == 0 or q.auditStatus == 1)'> "
			+ "   and ifnull(d.audit_status, 0) in (0, 1) "
			+ " </if>"
			+ " <if test='q.auditStatus != null and q.auditStatus != 0 and q.auditStatus != 1'> "
			+ "   and d.audit_status = #{q.auditStatus} "
			+ " </if>"
			+ " order by d.setTime desc, d.id desc"
			+ "</script>")
	List<DramaWorkAuditListRespEntity> findDramaList(@Param("q") DramaWorkAuditQuery q);

	/**
	 * 集评审列表。auditStatus 规则同剧列表。
	 */
	@Select("<script>"
			+ "select a.id as bizId, a.drama_id as dramaId, a.id as assetId, 2 as workType,"
			+ "  concat(ifnull(d.drama_title, ''), ' ', lpad(ifnull(a.set_num, 0), 2, '0')) as workName,"
			+ "  d.drama_title as dramaTitle,"
			+ "  d.cover_url as coverUrl, a.set_num as setNum, a.video_name as videoName, a.video_url as videoUrl,"
			+ "  a.audit_status as auditStatus, ifnull(a.shelf_status, 0) as shelfStatus,"
			+ "  a.audit_reject_reason as auditRejectReason,"
			+ "  ifnull(a.appeal_status, 0) as appealStatus, a.appeal_reason as appealReason, a.appeal_time as appealTime,"
			+ "  ifnull(ai.status, 0) as aiStatus, ai.handle_remark as aiHandleRemark,"
			+ "  ifnull(ga.status, 0) as groupAStatus, ga.handle_remark as groupAHandleRemark,"
			+ "  ifnull(gb.status, 0) as groupBStatus, gb.handle_remark as groupBHandleRemark,"
			+ "  a.setTime as uploadDate"
			+ " from drama_asset a"
			+ " left join drama d on d.id = a.drama_id"
			+ " left join drama_asset_audit_step ai on ai.asset_id = a.id and ai.step_type = 1"
			+ " left join drama_asset_audit_step ga on ga.asset_id = a.id and ga.step_type = 2"
			+ " left join drama_asset_audit_step gb on gb.asset_id = a.id and gb.step_type = 3"
			+ " where ifnull(a.delete_state, 0) = 0"
			+ " <if test='q.dramaId != null'> and a.drama_id = #{q.dramaId} </if>"
			+ " <if test='q.dramaTitle != null and q.dramaTitle != \"\"'> "
			+ "   and d.drama_title like concat('%', #{q.dramaTitle}, '%') "
			+ " </if>"
			+ " <if test='q.keyword != null and q.keyword != \"\"'> "
			+ "   and (d.drama_title like concat('%', #{q.keyword}, '%') "
			+ "     or concat(ifnull(d.drama_title, ''), ' ', lpad(ifnull(a.set_num, 0), 2, '0')) like concat('%', #{q.keyword}, '%')) "
			+ " </if>"
			+ " <if test='q.auditStatus != null and (q.auditStatus == 0 or q.auditStatus == 1)'> "
			+ "   and ifnull(a.audit_status, 0) in (0, 1) "
			+ " </if>"
			+ " <if test='q.auditStatus != null and q.auditStatus != 0 and q.auditStatus != 1'> "
			+ "   and a.audit_status = #{q.auditStatus} "
			+ " </if>"
			+ " order by a.setTime desc, a.id desc"
			+ "</script>")
	List<DramaWorkAuditListRespEntity> findEpisodeList(@Param("q") DramaWorkAuditQuery q);
}
