package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorSocialBindEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 创作者第三方账号绑定。
 */
@Repository
public interface CreatorSocialBindDao extends BaseMapper<CreatorSocialBindEntity> {

	@Select("select * from creator_social_bind where creator_id = #{creatorId} and provider = #{provider} limit 1")
	CreatorSocialBindEntity findByCreatorAndProvider(@Param("creatorId") Integer creatorId,
													 @Param("provider") String provider);
}
