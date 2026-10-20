package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.AppUserWatchHistoryItemEntity;
import com.playlet.internal.entity.drama.UserWatchHistoryEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWatchHistoryDao extends BaseMapper<UserWatchHistoryEntity> {

	@Select("select * from user_watch_history where uid = #{uid} order by gmtModified desc")
	List<UserWatchHistoryEntity> findByUid(@Param("uid") Integer uid);

	@Select("select * from user_watch_history where uid = #{uid} order by gmtModified desc limit #{limit}")
	List<UserWatchHistoryEntity> findByUidLimit(@Param("uid") Integer uid, @Param("limit") int limit);

	@Insert("insert into user_watch_history "
			+ "(uid, drama_id, episode_id, watch_progress, setTime, gmtModified) "
			+ "values (#{uid}, #{dramaId}, #{episodeId}, #{watchProgress}, #{setTime}, #{gmtModified}) "
			+ "on duplicate key update "
			+ "episode_id = values(episode_id), "
			+ "watch_progress = values(watch_progress), "
			+ "gmtModified = values(gmtModified)")
	int upsert(UserWatchHistoryEntity entity);

	@Delete("delete from user_watch_history where uid = #{uid} and drama_id = #{dramaId}")
	int deleteByUidAndDrama(@Param("uid") Integer uid, @Param("dramaId") Integer dramaId);

	@Delete("delete from user_watch_history where uid = #{uid}")
	int deleteByUid(@Param("uid") Integer uid);

	@Select("select ifnull(count(distinct drama_id), 0) from user_watch_history where uid = #{uid}")
	int countDistinctDramaByUid(@Param("uid") Integer uid);

	@Select("select h.drama_id as dramaId, d.drama_title as dramaName, h.episode_id as episodeId, "
			+ "h.watch_progress as watchProgress, h.gmtModified as gmtModified "
			+ "from user_watch_history h "
			+ "left join drama d on d.id = h.drama_id "
			+ "where h.uid = #{uid} "
			+ "order by h.gmtModified desc")
	List<AppUserWatchHistoryItemEntity> findAdminByUid(@Param("uid") Integer uid);
}
