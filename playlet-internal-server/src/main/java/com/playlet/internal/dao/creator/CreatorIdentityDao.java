package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorIdentityEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 创作者身份资质。
 */
@Repository
public interface CreatorIdentityDao extends BaseMapper<CreatorIdentityEntity> {

	@Select("select * from creator_identity where creator_id = #{creatorId} order by identity_type asc")
	List<CreatorIdentityEntity> findByCreatorId(@Param("creatorId") Integer creatorId);

	@Select("select * from creator_identity where creator_id = #{creatorId} and identity_type = #{identityType} limit 1")
	CreatorIdentityEntity findByCreatorAndType(@Param("creatorId") Integer creatorId,
											   @Param("identityType") Integer identityType);
}
