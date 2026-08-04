package com.playlet.internal.dao.drama;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.query.drama.QueryCommentVideoQuery;
import com.playlet.internal.query.drama.QueryDramaCommentQuery;

@Repository
public interface DramaVideoCommentDao extends BaseMapper<DramaVideoCommentEntity>{

	@Select("select dvc.*,aa.avatar as avatar from drama_video_comment dvc left join app_account aa on dvc.user_id = aa.id where video_id = #{voideId} "
			+ "and delete_state = #{deleteState} "
			+ "and parent_id = #{parentId} "
			+ "and (comment_type is null or comment_type = 1) "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> getList(QueryCommentVideoQuery entity);

	@Select("select dvc.*,aa.avatar as avatar from drama_video_comment dvc left join app_account aa on dvc.user_id = aa.id where drama_id = #{dramaId} "
			+ "and comment_type = 2 "
			+ "and delete_state = #{deleteState} "
			+ "and parent_id = #{parentId} "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> getDramaCommentList(QueryDramaCommentQuery entity);

	@Select("select dvc.*,aa.avatar as avatar from drama_video_comment dvc left join app_account aa on dvc.user_id = aa.id where parent_id = #{parentId} "
			+ "and delete_state = #{deleteState} "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> findParentId(@Param("parentId") Integer parentId,@Param("deleteState") Integer deleteState);

	@Select("select * from drama_video_comment where drama_id = #{dramaId} and user_id = #{userId} "
			+ "and comment_type = 2 and parent_id = 0 and delete_state = #{deleteState} "
			+ "order by id desc limit 1")
	DramaVideoCommentEntity findUserDramaComment(@Param("dramaId") Integer dramaId,
			@Param("userId") Integer userId,
			@Param("deleteState") Integer deleteState);

	@Select("select ifnull(avg(score),0) as avgScore, count(1) as scoreCount from drama_video_comment "
			+ "where drama_id = #{dramaId} and comment_type = 2 and parent_id = 0 "
			+ "and delete_state = #{deleteState} and score is not null")
	Map<String, Object> avgScoreByDramaId(@Param("dramaId") Integer dramaId,
			@Param("deleteState") Integer deleteState);

	@Select("select ifnull(avg(score),0) as avgScore from drama_video_comment "
			+ "where drama_id = #{dramaId} and comment_type = 2 "
			+ "and delete_state = #{deleteState} and score is not null")
	Double avgScoreNumByDramaId(@Param("dramaId") Integer dramaId, @Param("deleteState") Integer deleteState);

	@Select("select dvc.*,aa.avatar as avatar from drama_video_comment dvc "
			+ "left join app_account aa on dvc.user_id = aa.id where dvc.id = #{id}")
	DramaVideoCommentEntity findByIdWithAvatar(@Param("id") Integer id);

	@Select("<script>"
			+ "select * from drama_video_comment where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaVideoCommentEntity> findByIds(@Param("ids") List<Integer> ids);

	/**
	 * 一级视频评论中，比目标更新的条数（setTime desc, id desc），用于算页码。
	 */
	@Select("select count(1) from drama_video_comment where video_id = #{videoId} "
			+ "and delete_state = #{deleteState} and parent_id = 0 "
			+ "and (comment_type is null or comment_type = 1) "
			+ "and (setTime > #{setTime} or (setTime = #{setTime} and id > #{id}))")
	Integer countNewerLevel1(@Param("videoId") Integer videoId,
			@Param("deleteState") Integer deleteState,
			@Param("setTime") java.util.Date setTime,
			@Param("id") Integer id);

	/**
	 * 同 parent 下二级回复中，比目标更新的条数。
	 */
	@Select("select count(1) from drama_video_comment where parent_id = #{parentId} "
			+ "and delete_state = #{deleteState} "
			+ "and (setTime > #{setTime} or (setTime = #{setTime} and id > #{id}))")
	Integer countNewerReplies(@Param("parentId") Integer parentId,
			@Param("deleteState") Integer deleteState,
			@Param("setTime") java.util.Date setTime,
			@Param("id") Integer id);

	/**
	 * 一级短剧评论中，比目标更新的条数（setTime desc, id desc）。
	 */
	@Select("select count(1) from drama_video_comment where drama_id = #{dramaId} "
			+ "and comment_type = 2 and delete_state = #{deleteState} and parent_id = 0 "
			+ "and (setTime > #{setTime} or (setTime = #{setTime} and id > #{id}))")
	Integer countNewerDramaLevel1(@Param("dramaId") Integer dramaId,
			@Param("deleteState") Integer deleteState,
			@Param("setTime") java.util.Date setTime,
			@Param("id") Integer id);
}
