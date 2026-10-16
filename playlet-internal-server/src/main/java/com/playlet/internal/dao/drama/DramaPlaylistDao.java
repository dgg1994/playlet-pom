package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaPlaylistEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧单 Dao。
 */
@Repository
public interface DramaPlaylistDao extends BaseMapper<DramaPlaylistEntity> {

	@Select("select * from drama_playlist where status = 1 "
			+ "and langue = #{langue} "
			+ "order by sort_weight desc, id asc")
	List<DramaPlaylistEntity> findEnabledByLangue(@Param("langue") String langue);

	@Select("select * from drama_playlist where status = 1 "
			+ "and group_id = #{groupId} and langue = #{langue} limit 1")
	DramaPlaylistEntity findEnabledByGroupIdAndLangue(@Param("groupId") String groupId,
			@Param("langue") String langue);

	@Select("select * from drama_playlist where status = 1 "
			+ "and group_id = #{groupId} order by id asc limit 1")
	DramaPlaylistEntity findOneEnabledByGroupId(@Param("groupId") String groupId);

	@Select("select * from drama_playlist where playlist_name = #{playlistName} "
			+ "and langue = #{langue} limit 1")
	DramaPlaylistEntity findByNameAndLangue(@Param("playlistName") String playlistName,
			@Param("langue") String langue);

	@Select("select * from drama_playlist where group_id = #{groupId}")
	List<DramaPlaylistEntity> selectByGroupId(@Param("groupId") String groupId);

	@Select("<script>"
			+ "select * from drama_playlist where 1=1 "
			+ "<if test='groupId != null and groupId != \"\"'> and group_id = #{groupId} </if>"
			+ "<if test='langue != null and langue != \"\"'> and langue = #{langue} </if>"
			+ "<if test='playlistName != null and playlistName != \"\"'> "
			+ "  and playlist_name like concat('%',#{playlistName},'%') "
			+ "</if>"
			+ "<if test='matchMode != null'> and match_mode = #{matchMode} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<DramaPlaylistEntity> findAdminList(DramaPlaylistEntity entity);

	@Update("update drama_playlist set status = #{status}, gmtModified = NOW() "
			+ "where group_id = #{groupId}")
	int updateStatusByGroupId(@Param("groupId") String groupId, @Param("status") Integer status);
}
