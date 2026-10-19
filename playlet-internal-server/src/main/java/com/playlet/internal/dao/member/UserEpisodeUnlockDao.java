package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.member.UserEpisodeUnlockEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户剧集解锁 Dao。
 */
@Repository
public interface UserEpisodeUnlockDao extends BaseMapper<UserEpisodeUnlockEntity> {

	@Select("select * from user_episode_unlock where uid = #{uid} and asset_id = #{assetId} limit 1")
	UserEpisodeUnlockEntity findByUidAndAssetId(@Param("uid") Integer uid,
			@Param("assetId") Integer assetId);

	@Select("select count(1) from user_episode_unlock where uid = #{uid} and asset_id = #{assetId}")
	int countByUidAndAssetId(@Param("uid") Integer uid, @Param("assetId") Integer assetId);

	@Select("<script>"
			+ "select * from user_episode_unlock where uid = #{uid} "
			+ "<if test='dramaId != null'> and drama_id = #{dramaId} </if>"
			+ "order by id desc"
			+ "</script>")
	List<UserEpisodeUnlockEntity> findListByUid(@Param("uid") Integer uid,
			@Param("dramaId") Integer dramaId);
}
