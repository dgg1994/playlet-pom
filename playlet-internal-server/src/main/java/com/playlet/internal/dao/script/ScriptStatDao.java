package com.playlet.internal.dao.script;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.script.ScriptStatEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

/**
 * 剧本计数表：原子增减。
 */
@Repository
public interface ScriptStatDao extends BaseMapper<ScriptStatEntity> {

	@Insert("insert ignore into script_stat(script_id, view_count, favorite_count, apply_count, deal_count, gmtModified) "
			+ "values(#{scriptId}, 0, 0, 0, 0, now())")
	int initIfAbsent(@Param("scriptId") Integer scriptId);

	@Update("update script_stat set view_count = ifnull(view_count,0) + 1, gmtModified = now() "
			+ "where script_id = #{scriptId}")
	int incrView(@Param("scriptId") Integer scriptId);

	@Update("update script_stat set favorite_count = greatest(ifnull(favorite_count,0) + #{delta}, 0), "
			+ "gmtModified = now() where script_id = #{scriptId}")
	int incrFavorite(@Param("scriptId") Integer scriptId, @Param("delta") int delta);

	@Update("update script_stat set apply_count = ifnull(apply_count,0) + 1, gmtModified = now() "
			+ "where script_id = #{scriptId}")
	int incrApply(@Param("scriptId") Integer scriptId);

	@Update("update script_stat set deal_count = ifnull(deal_count,0) + 1, gmtModified = now() "
			+ "where script_id = #{scriptId}")
	int incrDeal(@Param("scriptId") Integer scriptId);

	@Select("select * from script_stat where script_id = #{scriptId} limit 1")
	ScriptStatEntity findByScriptId(@Param("scriptId") Integer scriptId);
}
