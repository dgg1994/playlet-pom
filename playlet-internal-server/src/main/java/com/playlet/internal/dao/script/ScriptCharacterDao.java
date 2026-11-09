package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptCharacterEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧本人物介绍。
 */
@Repository
public interface ScriptCharacterDao extends BaseMapper<ScriptCharacterEntity> {

	@Delete("delete from script_character where script_id = #{scriptId}")
	int deleteByScriptId(@Param("scriptId") Integer scriptId);

	@Insert("<script>insert into script_character(script_id, char_name, char_role, avatar_url, description, sort_order, setTime, gmtModified) values "
			+ "<foreach collection='list' item='it' separator=','>"
			+ "(#{it.scriptId}, #{it.charName}, #{it.charRole}, #{it.avatarUrl}, #{it.description}, #{it.sortOrder}, now(), now())"
			+ "</foreach></script>")
	int batchInsert(@Param("list") List<ScriptCharacterEntity> list);

	@Select("select * from script_character where script_id = #{scriptId} order by sort_order asc, id asc")
	List<ScriptCharacterEntity> findByScriptId(@Param("scriptId") Integer scriptId);
}
