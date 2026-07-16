package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.UserWatchHistoryEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWatchHistoryDao extends BaseMapper<UserWatchHistoryEntity> {

	@Select("select * from user_watch_history where uid = #{uid} and drama_id = #{dramaId} limit 1")
	UserWatchHistoryEntity findByUidAndDrama(@Param("uid") String uid, @Param("dramaId") String dramaId);

	@Select("select * from user_watch_history where uid = #{uid} order by gmtModified desc")
	List<UserWatchHistoryEntity> findByUid(@Param("uid") String uid);

	@Select("select * from user_watch_history where uid = #{uid} order by gmtModified desc limit #{limit}")
	List<UserWatchHistoryEntity> findByUidLimit(@Param("uid") String uid, @Param("limit") int limit);

	@Insert("insert into user_watch_history "
			+ "(uid, drama_id, episode_id, episode_no, watch_progress, setTime, gmtModified) "
			+ "values (#{uid}, #{dramaId}, #{episodeId}, #{episodeNo}, #{watchProgress}, #{setTime}, #{gmtModified}) "
			+ "on duplicate key update "
			+ "episode_id = values(episode_id), "
			+ "episode_no = values(episode_no), "
			+ "watch_progress = values(watch_progress), "
			+ "gmtModified = values(gmtModified)")
	int upsert(UserWatchHistoryEntity entity);

	@Delete("delete from user_watch_history where uid = #{uid} and drama_id = #{dramaId}")
	int deleteByUidAndDrama(@Param("uid") String uid, @Param("dramaId") Integer dramaId);

	@Delete("delete from user_watch_history where uid = #{uid}")
	int deleteByUid(@Param("uid") String uid);
}
