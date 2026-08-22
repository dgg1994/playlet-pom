package com.playlet.internal.dao.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.ops.UserPlayDailyEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPlayDailyDao extends BaseMapper<UserPlayDailyEntity> {

	/** 累计当日有效播放秒 */
	@Insert("insert into user_play_daily (biz_date, uid, play_seconds, setTime, gmtModified) "
			+ "values (#{bizDate}, #{uid}, #{deltaSeconds}, now(), now()) "
			+ "on duplicate key update "
			+ "play_seconds = play_seconds + values(play_seconds), gmtModified = now()")
	int addPlaySeconds(@Param("bizDate") String bizDate, @Param("uid") Integer uid,
			@Param("deltaSeconds") int deltaSeconds);

	@Select("select ifnull(sum(play_seconds), 0) from user_play_daily "
			+ "where biz_date >= #{startDate} and biz_date <= #{endDate}")
	long sumPlaySeconds(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
