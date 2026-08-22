package com.playlet.oversea.dao.creator;

import com.playlet.oversea.api.response.CreatorCommentListRow;
import com.playlet.oversea.query.creator.CreatorCommentListQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作家端评论管理查询。
 */
@Repository
public interface CreatorCommentDao {

	/**
	 * 第一步：本人作品下评论分页（仅 join drama + asset）；按 commentType 区分集评/剧评。
	 */
	@Select("<script>"
			+ "select dvc.id, dvc.drama_id as dramaId, d.drama_title as dramaTitle, "
			+ "dvc.video_id as videoId, a.set_num as setNum, dvc.parent_id as parentId, "
			+ "dvc.user_id as userId, dvc.from_creator_id as fromCreatorId, "
			+ "dvc.comment_info as commentInfo, ifnull(dvc.like_count, 0) as likeCount, "
			+ "ifnull(dvc.pin_flag, 0) as pinFlag, dvc.pin_time as pinTime, dvc.setTime as setTime, "
			+ "ifnull(dvc.comment_type, 1) as commentType "
			+ "from drama_video_comment dvc "
			+ "inner join drama d on d.id = dvc.drama_id "
			+ "  and d.belong_user = #{creatorId} and ifnull(d.delete_state, 0) = 0 "
			+ "left join drama_asset a on a.id = dvc.video_id "
			+ "where ifnull(dvc.delete_state, 0) = 0 "
			+ "<choose>"
			+ "  <when test='q.commentType != null and q.commentType == 2'> "
			+ "    and dvc.comment_type = 2 "
			+ "  </when>"
			+ "  <otherwise> "
			+ "    and (dvc.comment_type is null or dvc.comment_type = 1) "
			+ "    and ifnull(dvc.video_id, 0) &gt; 0 "
			+ "  </otherwise>"
			+ "</choose>"
			+ "<if test='q.dramaId != null'> "
			+ "  and dvc.drama_id = #{q.dramaId} "
			+ "</if>"
			+ "<if test='q.dramaTitle != null and q.dramaTitle != \"\"'> "
			+ "  and d.drama_title like concat('%', #{q.dramaTitle}, '%') "
			+ "</if>"
			+ "order by ifnull(dvc.pin_flag, 0) desc, "
			+ "case when ifnull(dvc.pin_flag, 0) = 1 then dvc.pin_time end desc, "
			+ "<choose>"
			+ "  <when test='q.sortType != null and q.sortType == 1'> ifnull(dvc.like_count, 0) desc, dvc.id desc </when>"
			+ "  <otherwise> dvc.setTime desc, dvc.id desc </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<CreatorCommentListRow> findList(@Param("q") CreatorCommentListQuery query,
			@Param("creatorId") Integer creatorId);
}
