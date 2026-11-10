package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorStatEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 创作者统计。
 */
@Repository
public interface CreatorStatDao extends BaseMapper<CreatorStatEntity> {

	@Select("select * from creator_stat where creator_id = #{creatorId} limit 1")
	CreatorStatEntity findByCreatorId(@Param("creatorId") Integer creatorId);
}
