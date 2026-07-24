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

	@Select("select * from drama_video_comment where video_id = #{voideId} "
			+ "and delete_state = #{deleteState} "
			+ "and parent_id = #{parentId} "
			+ "and (comment_type is null or comment_type = 1) "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> getList(QueryCommentVideoQuery entity);

	@Select("select * from drama_video_comment where drama_id = #{dramaId} "
			+ "and comment_type = 2 "
			+ "and delete_state = #{deleteState} "
			+ "and parent_id = #{parentId} "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> getDramaCommentList(QueryDramaCommentQuery entity);

	@Select("select * from drama_video_comment where parent_id = #{parentId} "
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
}
