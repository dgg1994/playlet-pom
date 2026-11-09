package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptFavoriteEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 用户收藏。
 */
@Repository
public interface ScriptFavoriteDao extends BaseMapper<ScriptFavoriteEntity> {

	@Insert("insert ignore into script_favorite(uid, script_id, setTime, gmtModified) "
			+ "values(#{uid}, #{scriptId}, now(), now())")
	int insertIgnore(@Param("uid") Integer uid, @Param("scriptId") Integer scriptId);

	@Delete("delete from script_favorite where uid = #{uid} and script_id = #{scriptId}")
	int deleteByUidAndScriptId(@Param("uid") Integer uid, @Param("scriptId") Integer scriptId);

	@Select("select * from script_favorite where uid = #{uid} and script_id = #{scriptId} limit 1")
	ScriptFavoriteEntity findByUidAndScriptId(@Param("uid") Integer uid, @Param("scriptId") Integer scriptId);
}
