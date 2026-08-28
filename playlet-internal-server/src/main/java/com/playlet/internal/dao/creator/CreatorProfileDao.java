package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorProfileEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 作家入驻资料。
 */
@Repository
public interface CreatorProfileDao extends BaseMapper<CreatorProfileEntity> {

	@Select("select * from creator_profile where creator_id = #{creatorId} limit 1")
	CreatorProfileEntity findByCreatorId(@Param("creatorId") Integer creatorId);
}
