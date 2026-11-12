package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCallSubmissionEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 征稿投稿记录查询。
 */
@Repository
public interface CreatorCallSubmissionDao extends BaseMapper<CreatorCallSubmissionEntity> {

	/** 指定征稿的全部有效投稿，按投稿时间倒序 */
	@Select("select * from creator_call_submission where call_id = #{callId} "
			+ "and ifnull(delete_state,0) = 0 order by setTime desc, id desc")
	List<CreatorCallSubmissionEntity> findByCallId(@Param("callId") Integer callId);

	/** 指定创作者是否已投稿指定征稿（同一征稿仅允许投稿一次） */
	@Select("select count(1) from creator_call_submission where call_id = #{callId} "
			+ "and creator_id = #{creatorId} and ifnull(delete_state,0) = 0")
	int countByCallAndCreator(@Param("callId") Integer callId, @Param("creatorId") Integer creatorId);
}
