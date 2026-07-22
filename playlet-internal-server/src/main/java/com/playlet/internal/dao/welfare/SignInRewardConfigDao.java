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
}
