package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.UserDramaLikeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDramaLikeDao extends BaseMapper<UserDramaLikeEntity> {

	@Select("select * from user_drama_like where uid = #{uid} and drama_id = #{dramaId} "
			+ "and like_type = #{likeType} and episode_id = #{episodeId} limit 1")
	UserDramaLikeEntity findOne(@Param("uid") Integer uid, @Param("dramaId") Integer dramaId,
			@Param("likeType") Integer likeType, @Param("episodeId") String episodeId);

	@Select("select * from user_drama_like where uid = #{uid} and drama_id = #{dramaId} "
			+ "and like_type = 1 and episode_id = '' limit 1")
	UserDramaLikeEntity findDramaLike(@Param("uid") String uid, @Param("dramaId") Integer dramaId);

	@Select("select * from user_drama_like where uid = #{uid} and drama_id = #{dramaId} "
			+ "and like_type = 2 and episode_id = #{episodeId} limit 1")
	UserDramaLikeEntity findEpisodeLike(@Param("uid") String uid, @Param("dramaId") Integer dramaId,
			@Param("episodeId") String episodeId);

	@Select("<script>"
			+ "select * from user_drama_like where uid = #{uid} "
			+ "<if test='likeType != null'> and like_type = #{likeType} </if>"
			+ "order by setTime desc"
			+ "</script>")
	List<UserDramaLikeEntity> findByUid(@Param("uid") Integer uid, @Param("likeType") Integer likeType);

	@Insert("insert into user_drama_like (uid, drama_id, like_type, episode_id, setTime, gmtModified) "
			+ "values (#{uid}, #{dramaId}, #{likeType}, #{episodeId}, #{setTime}, #{gmtModified}) "
			+ "on duplicate key update gmtModified = values(gmtModified)")
	int upsert(UserDramaLikeEntity entity);

	@Delete("delete from user_drama_like where uid = #{uid} and drama_id = #{dramaId} "
			+ "and like_type = #{likeType} and episode_id = #{episodeId}")
	int deleteOne(@Param("uid") Integer uid, @Param("dramaId") Integer dramaId,
			@Param("likeType") Integer likeType, @Param("episodeId") String episodeId);

	@Select("select count(*) from user_drama_like where drama_id = #{dramaId}")
	long countByDramaId(@Param("dramaId") Integer dramaId);

	@Select("select count(*) from user_drama_like where drama_id = #{dramaId} "
			+ "and like_type = 2 and episode_id = #{episodeId}")
	long countByEpisode(@Param("dramaId") Integer dramaId, @Param("episodeId") String episodeId);

	@Select("select count(*) from user_drama_like where uid = #{uid}")
	Long countLike(@Param("uid") Integer uid);

	@Select("select * from user_drama_like where episode_id = #{voideId}")
	UserDramaLikeEntity findByVoideId(@Param("voideId") Integer voideId);
}
