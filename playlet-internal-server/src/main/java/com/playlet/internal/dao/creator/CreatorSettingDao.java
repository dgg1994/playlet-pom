package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorSettingEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 创作者隐私与推送设置。
 */
@Repository
public interface CreatorSettingDao extends BaseMapper<CreatorSettingEntity> {

	@Select("select * from creator_setting where creator_id = #{creatorId} limit 1")
	CreatorSettingEntity findByCreatorId(@Param("creatorId") Integer creatorId);
}
