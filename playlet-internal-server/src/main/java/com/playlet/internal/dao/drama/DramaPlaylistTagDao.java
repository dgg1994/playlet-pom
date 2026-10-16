package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaPlaylistTagEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧单标签关联 Dao。
 */
@Repository
public interface DramaPlaylistTagDao extends BaseMapper<DramaPlaylistTagEntity> {

	@Select("select tag_group_id from drama_playlist_tag "
			+ "where playlist_group_id = #{playlistGroupId} "
			+ "order by sort_weight desc, id asc")
	List<String> findTagGroupIds(@Param("playlistGroupId") String playlistGroupId);

	@Delete("delete from drama_playlist_tag where playlist_group_id = #{playlistGroupId}")
	int deleteByPlaylistGroupId(@Param("playlistGroupId") String playlistGroupId);
}
