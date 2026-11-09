package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.ScriptTagRow;
import com.playlet.internal.entity.script.ScriptTagRelEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧本标签关联。
 */
@Repository
public interface ScriptTagRelDao extends BaseMapper<ScriptTagRelEntity> {

	@Delete("delete from script_tag_rel where script_id = #{scriptId}")
	int deleteByScriptId(@Param("scriptId") Integer scriptId);

	@Insert("<script>insert into script_tag_rel(script_id, tag_id, sort_order, setTime) values "
			+ "<foreach collection='list' item='it' separator=','>"
			+ "(#{it.scriptId}, #{it.tagId}, #{it.sortOrder}, now())"
			+ "</foreach></script>")
	int batchInsert(@Param("list") List<ScriptTagRelEntity> list);

	@Select("<script>select r.script_id as scriptId, t.tag_name as tagName "
			+ "from script_tag_rel r join dic_script_tag t on t.id = r.tag_id "
			+ "where r.script_id in "
			+ "<foreach collection='scriptIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach> "
			+ "order by r.sort_order asc, r.id asc</script>")
	List<ScriptTagRow> findTagsByScriptIds(@Param("scriptIds") List<Integer> scriptIds);

	@Select("select t.tag_name from script_tag_rel r join dic_script_tag t on t.id = r.tag_id "
			+ "where r.script_id = #{scriptId} order by r.sort_order asc, r.id asc")
	List<String> findTagNamesByScriptId(@Param("scriptId") Integer scriptId);
}
