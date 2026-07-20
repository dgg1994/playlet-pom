package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.UserWelfareWatchLogEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWelfareWatchLogDao extends BaseMapper<UserWelfareWatchLogEntity> {

	@Select("select * from user_welfare_watch_log where uid = #{uid} and drama_id = #{dramaId} "
			+ "and episode_id = #{episodeId} and biz_date = #{bizDate} limit 1")
	UserWelfareWatchLogEntity findOne(@Param("uid") String uid, @Param("dramaId") Integer dramaId,
			@Param("episodeId") String episodeId, @Param("bizDate") String bizDate);

	@Insert("insert ignore into user_welfare_watch_log (uid, drama_id, episode_id, biz_date, setTime) "
			+ "values (#{uid}, #{dramaId}, #{episodeId}, #{bizDate}, #{setTime})")
	int insertIgnore(UserWelfareWatchLogEntity entity);
}
