package com.playlet.internal.dao.ad;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.ad.AdRewardLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 激励广告流水 Dao。
 */
@Repository
public interface AdRewardLogDao extends BaseMapper<AdRewardLogEntity> {

	@Select("select * from ad_reward_log where trans_id = #{transId} limit 1")
	AdRewardLogEntity findByTransId(@Param("transId") String transId);

	@Select("select count(1) from ad_reward_log where verify_ok = 1 "
			+ "and setTime >= #{startTime} and setTime &lt; #{endTime}")
	long countVerifiedBetween(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
