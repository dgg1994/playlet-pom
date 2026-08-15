package com.playlet.internal.dao.creator;

import com.playlet.internal.api.response.CreatorCommentListRow;
import com.playlet.internal.query.creator.CreatorCommentListQuery;
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
	 * 本人作品下的集评/回复；置顶优先，再按热度或时间。
	 */
	@Select("<script>"
			+ "select dvc.id, dvc.drama_id as dramaId, d.drama_title as dramaTitle, "
			+ "dvc.video_id as videoId, a.set_num as setNum, dvc.parent_id as parentId, "
			+ "dvc.user_id as userId, dvc.from_creator_id as fromCreatorId, "
			+ "case when dvc.from_creator_id is not null "
			+ "  then ifnull(nullif(ca.nickname, ''), ca.user_account) "
			+ "  else aa.nickname end as userName, "
			+ "case when dvc.from_creator_id is not null then ca.avatar else aa.avatar end as avatar, "
			+ "dvc.comment_info as commentInfo, ifnull(dvc.like_count, 0) as likeCount, "
			+ "ifnull(dvc.pin_flag, 0) as pinFlag, dvc.pin_time as pinTime, dvc.setTime as setTime, "
			+ "p.id as parentCommentId, "
			+ "case when p.from_creator_id is not null "
			+ "  then ifnull(nullif(pca.nickname, ''), pca.user_account) "
			+ "  else paa.nickname end as parentUserName, "
			+ "case when p.from_creator_id is not null then pca.avatar else paa.avatar end as parentAvatar, "
			+ "p.comment_info as parentCommentInfo, p.from_creator_id as parentFromCreatorId "
			+ "from drama_video_comment dvc "
			+ "inner join drama d on d.id = dvc.drama_id "
			+ "  and d.belong_user = #{creatorId} and ifnull(d.delete_state, 0) = 0 "
			+ "left join drama_asset a on a.id = dvc.video_id "
			+ "left join app_account aa on aa.id = dvc.user_id "
			+ "left join creator_account ca on ca.id = dvc.from_creator_id "
			+ "left join drama_video_comment p on p.id = dvc.parent_id and dvc.parent_id &gt; 0 "
			+ "left join app_account paa on paa.id = p.user_id "
			+ "left join creator_account pca on pca.id = p.from_creator_id "
			+ "where ifnull(dvc.delete_state, 0) = 0 "
			+ "and (dvc.comment_type is null or dvc.comment_type = 1) "
			+ "and ifnull(dvc.video_id, 0) &gt; 0 "
			+ "<if test='q.dramaTitle != null and q.dramaTitle != \"\"'> "
			+ "  and d.drama_title like concat('%', #{q.dramaTitle}, '%') "
			+ "</if>"
			+ "order by ifnull(dvc.pin_flag, 0) desc, dvc.pin_time desc, "
			+ "<choose>"
			+ "  <when test='q.sortType != null and q.sortType == 1'> ifnull(dvc.like_count, 0) desc, dvc.id desc </when>"
			+ "  <otherwise> dvc.setTime desc, dvc.id desc </otherwise>"
			+ "</choose>"
			+ "</script>")
	List<CreatorCommentListRow> findList(@Param("q") CreatorCommentListQuery query,
			@Param("creatorId") Integer creatorId);
}
