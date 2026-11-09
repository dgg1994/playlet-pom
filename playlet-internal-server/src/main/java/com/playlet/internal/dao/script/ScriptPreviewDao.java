package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptPreviewEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧本试读。
 */
@Repository
public interface ScriptPreviewDao extends BaseMapper<ScriptPreviewEntity> {

	@Delete("delete from script_preview where script_id = #{scriptId}")
	int deleteByScriptId(@Param("scriptId") Integer scriptId);

	@Insert("<script>insert into script_preview(script_id, episode_no, scene_no, scene_title, content_json, setTime, gmtModified) values "
			+ "<foreach collection='list' item='it' separator=','>"
			+ "(#{it.scriptId}, #{it.episodeNo}, #{it.sceneNo}, #{it.sceneTitle}, #{it.contentJson}, now(), now())"
			+ "</foreach></script>")
	int batchInsert(@Param("list") List<ScriptPreviewEntity> list);

	@Select("select * from script_preview where script_id = #{scriptId} order by episode_no asc, scene_no asc, id asc")
	List<ScriptPreviewEntity> findByScriptId(@Param("scriptId") Integer scriptId);
}
