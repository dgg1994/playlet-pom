package com.playlet.internal.dao.creator;

import com.playlet.internal.api.response.CreatorDramaListRespEntity;
import com.playlet.internal.query.creator.CreatorDramaListQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作家端作品查询（强制 belong_user）。
 */
@Repository
public interface CreatorDramaDao {

	/**
	 * 当前作家的剧列表。auditStatus：0/1 待审核（含待审+审核中），其余精确匹配。
	 */
	@Select("<script>"
			+ "select d.id as id, d.drama_title as dramaTitle, d.cover_url as coverUrl,"
			+ "  d.total_episodes as totalEpisodes, d.finished_state as finishedState, d.video_type as videoType,"
			+ "  d.audit_status as auditStatus, ifnull(d.shelf_status, 0) as shelfStatus,"
			+ "  d.audit_reject_reason as auditRejectReason, ifnull(d.appeal_status, 0) as appealStatus,"
			+ "  d.setTime as setTime,"
			+ "  (select ifnull(count(*), 0) from drama_asset a"
			+ "    where a.drama_id = d.id and ifnull(a.delete_state, 0) = 0) as uploadSetNum"
			+ " from drama d"
			+ " where ifnull(d.delete_state, 0) = 0"
			+ "   and d.belong_user = #{belongUser}"
			+ " <if test='q.dramaTitle != null and q.dramaTitle != \"\"'> "
			+ "   and d.drama_title like concat('%', #{q.dramaTitle}, '%') "
			+ " </if>"
			+ " <if test='q.auditStatus != null and (q.auditStatus == 0 or q.auditStatus == 1)'> "
			+ "   and ifnull(d.audit_status, 0) in (0, 1) "
			+ " </if>"
			+ " <if test='q.auditStatus != null and q.auditStatus != 0 and q.auditStatus != 1'> "
			+ "   and d.audit_status = #{q.auditStatus} "
			+ " </if>"
			+ " <if test='q.shelfStatus != null'> and ifnull(d.shelf_status, 0) = #{q.shelfStatus} </if>"
			+ " order by d.setTime desc, d.id desc"
			+ "</script>")
	List<CreatorDramaListRespEntity> findList(@Param("q") CreatorDramaListQuery q,
			@Param("belongUser") Integer belongUser);
}
