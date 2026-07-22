package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WatchGiftRewardConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchGiftRewardConfigDao extends BaseMapper<WatchGiftRewardConfigEntity> {

	@Select("select * from watch_gift_reward_config where status = 1 order by target_seconds asc, gear_index asc")
	List<WatchGiftRewardConfigEntity> findEnabledList();

	@Select("select * from watch_gift_reward_config where status = 1 and gear_index = #{gearIndex} limit 1")
	WatchGiftRewardConfigEntity findEnabledByGear(@Param("gearIndex") Integer gearIndex);
}
