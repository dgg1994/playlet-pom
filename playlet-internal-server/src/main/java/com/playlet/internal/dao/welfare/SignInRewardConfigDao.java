package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.SignInRewardConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignInRewardConfigDao extends BaseMapper<SignInRewardConfigEntity> {

	@Select("select * from sign_in_reward_config where status = 1 order by day_index asc")
	List<SignInRewardConfigEntity> findEnabledList();

	@Select("select * from sign_in_reward_config where status = 1 and day_index = #{dayIndex} limit 1")
	SignInRewardConfigEntity findByDayIndex(@Param("dayIndex") Integer dayIndex);

	@Select("select ifnull(max(day_index), 0) from sign_in_reward_config where status = 1")
	int findMaxDayIndex();

	/** 管理端：同 day_index 是否已存在（排除自身，用于唯一校验） */
	@Select("<script>"
			+ "select count(1) from sign_in_reward_config where day_index = #{dayIndex} "
			+ "<if test='excludeId != null'> and id &lt;&gt; #{excludeId} </if>"
			+ "</script>")
	int countByDayIndex(@Param("dayIndex") Integer dayIndex, @Param("excludeId") Integer excludeId);
}
