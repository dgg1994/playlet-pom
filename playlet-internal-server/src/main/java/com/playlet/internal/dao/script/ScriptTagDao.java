package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptTagEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 剧本标签字典。
 */
@Repository
public interface ScriptTagDao extends BaseMapper<ScriptTagEntity> {

	@Select("select * from dic_script_tag where group_id = #{groupId} and langue = #{langue} "
			+ "and status = 1 order by sort_weight desc, id asc")
	List<ScriptTagEntity> findByGroup(@Param("groupId") String groupId, @Param("langue") String langue);

	@Select("select * from dic_script_tag where group_id = #{groupId} and langue = #{langue} "
			+ "and tag_name = #{tagName} limit 1")
	ScriptTagEntity findByName(@Param("groupId") String groupId,
			@Param("langue") String langue,
			@Param("tagName") String tagName);

	@Insert("insert ignore into dic_script_tag(group_id, langue, tag_name, sort_weight, status, setTime, gmtModified) "
			+ "values(#{groupId}, #{langue}, #{tagName}, 0, 1, now(), now())")
	int insertIgnore(@Param("groupId") String groupId,
			@Param("langue") String langue,
			@Param("tagName") String tagName);
}
