package com.playlet.internal.dao.drama;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaVideoCommentEntity;
import com.playlet.internal.query.drama.QueryCommentVideoQuery;

@Repository
public interface DramaVideoCommentDao extends BaseMapper<DramaVideoCommentEntity>{

	@Select("select * from drama_video_comment where video_id = #{voideId} "
			+ "and delete_state = #{deleteState} "
			+ "and parent_id = #{parentId} "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> getList(QueryCommentVideoQuery entity);

	@Select("select * from drama_video_comment where parent_id = #{parentId} "
			+ "and delete_state = #{deleteState} "
			+ "order by setTime desc")
	List<DramaVideoCommentEntity> findParentId(@Param("parentId") Integer parentId,@Param("deleteState") Integer deleteState);

}
