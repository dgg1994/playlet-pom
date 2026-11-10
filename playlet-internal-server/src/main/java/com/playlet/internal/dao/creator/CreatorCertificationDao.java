package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCertificationEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 创作者认证申请。
 */
@Repository
public interface CreatorCertificationDao extends BaseMapper<CreatorCertificationEntity> {

	@Select("select * from creator_certification where creator_id = #{creatorId} and category = #{category} limit 1")
	CreatorCertificationEntity findByCreatorAndCategory(@Param("creatorId") Integer creatorId,
														@Param("category") Integer category);
}
