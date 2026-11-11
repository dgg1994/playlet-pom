package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCallJoinEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 征稿参与记录查询。
 */
@Repository
public interface CreatorCallJoinDao extends BaseMapper<CreatorCallJoinEntity> {

	/** 当前创作者已参与的征稿 id 列表 */
	@Select("select call_id from creator_call_join where creator_id = #{creatorId}")
	List<Integer> findJoinedCallIds(@Param("creatorId") Integer creatorId);

	/** 指定创作者是否已参与指定征稿 */
	@Select("select count(1) from creator_call_join where call_id = #{callId} and creator_id = #{creatorId}")
	int countJoin(@Param("callId") Integer callId, @Param("creatorId") Integer creatorId);
}
