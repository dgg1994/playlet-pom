package com.playlet.internal.dao.drama;

import com.playlet.internal.query.drama.DramaWorkAuditQuery;
import com.playlet.internal.api.response.DramaWorkAuditListRespEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaWorkAuditDao {

	/**
	 * 作品管理列表（仅剧）：含封面 + AI/A/B 步骤状态。
	 * listTab: 1待审(0/1) 2申诉(暂无数据) 3完审(2) 4驳回(3)
	 */
	@Select("<script>"
			+ "select d.id as bizId, d.id as dramaId, cast(null as signed) as assetId, 1 as workType,"
			+ "  d.drama_title as workName, d.cover_url as coverUrl, d.audit_status as auditStatus,"
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
			+ " <if test='q.listTab != null and q.listTab == 1'> and ifnull(d.audit_status, 0) in (0, 1) </if>"
			+ " <if test='q.listTab != null and q.listTab == 2'> and 1 = 0 </if>"
			+ " <if test='q.listTab != null and q.listTab == 3'> and d.audit_status = 2 </if>"
			+ " <if test='q.listTab != null and q.listTab == 4'> and d.audit_status = 3 </if>"
			+ " <if test='q.auditStatus != null'> and d.audit_status = #{q.auditStatus} </if>"
			+ " order by d.setTime desc, d.id desc"
			+ "</script>")
	List<DramaWorkAuditListRespEntity> findDramaList(@Param("q") DramaWorkAuditQuery q);

	/**
	 * 集评审列表：剧名检索、审核状态/页签筛选，附带 AI/A/B 步骤状态。
	 */
	@Select("<script>"
			+ "select a.id as bizId, a.drama_id as dramaId, a.id as assetId, 2 as workType,"
			+ "  concat(ifnull(d.drama_title, ''), ' ', lpad(ifnull(a.set_num, 0), 2, '0')) as workName,"
			+ "  d.drama_title as dramaTitle,"
			+ "  d.cover_url as coverUrl, a.set_num as setNum, a.video_name as videoName, a.video_url as videoUrl,"
			+ "  a.audit_status as auditStatus,"
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
			+ " <if test='q.listTab != null and q.listTab == 1'> and ifnull(a.audit_status, 0) in (0, 1) </if>"
			+ " <if test='q.listTab != null and q.listTab == 2'> and 1 = 0 </if>"
			+ " <if test='q.listTab != null and q.listTab == 3'> and a.audit_status = 2 </if>"
			+ " <if test='q.listTab != null and q.listTab == 4'> and a.audit_status = 3 </if>"
			+ " <if test='q.auditStatus != null'> and a.audit_status = #{q.auditStatus} </if>"
			+ " order by a.setTime desc, a.id desc"
			+ "</script>")
	List<DramaWorkAuditListRespEntity> findEpisodeList(@Param("q") DramaWorkAuditQuery q);
}
