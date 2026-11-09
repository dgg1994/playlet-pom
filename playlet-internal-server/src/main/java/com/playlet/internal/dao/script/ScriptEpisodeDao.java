package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptEpisodeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧本分集结构。
 */
@Repository
public interface ScriptEpisodeDao extends BaseMapper<ScriptEpisodeEntity> {

	@Delete("delete from script_episode where script_id = #{scriptId}")
	int deleteByScriptId(@Param("scriptId") Integer scriptId);

	@Insert("<script>insert into script_episode(script_id, episode_no, episode_title, duration_seconds, setTime, gmtModified) values "
			+ "<foreach collection='list' item='it' separator=','>"
			+ "(#{it.scriptId}, #{it.episodeNo}, #{it.episodeTitle}, #{it.durationSeconds}, now(), now())"
			+ "</foreach></script>")
	int batchInsert(@Param("list") List<ScriptEpisodeEntity> list);

	@Select("select * from script_episode where script_id = #{scriptId} order by episode_no asc, id asc")
	List<ScriptEpisodeEntity> findByScriptId(@Param("scriptId") Integer scriptId);
}
