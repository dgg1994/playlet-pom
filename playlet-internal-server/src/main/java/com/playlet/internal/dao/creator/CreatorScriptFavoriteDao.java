package com.playlet.internal.dao.creator;

import com.playlet.internal.api.response.ScriptFavoriteItemRespEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 创作者剧本收藏。
 */
@Repository
public interface CreatorScriptFavoriteDao {

	/** 剧本是否存在（未删除） */
	@Select("select count(1) from script where id = #{scriptId} and ifnull(delete_state, 0) = 0")
	int countScript(@Param("scriptId") Integer scriptId);

	/** 收藏（重复收藏忽略） */
	@Insert("insert ignore into creator_script_favorite(creator_id, script_id, setTime, gmtModified) "
			+ "values(#{creatorId}, #{scriptId}, now(), now())")
	int insertIgnore(@Param("creatorId") Integer creatorId, @Param("scriptId") Integer scriptId);

	/** 取消收藏 */
	@Delete("delete from creator_script_favorite where creator_id = #{creatorId} and script_id = #{scriptId}")
	int deleteByCreatorAndScript(@Param("creatorId") Integer creatorId, @Param("scriptId") Integer scriptId);

	/**
	 * 剧本收藏列表：剧本基础信息 + 作者昵称。
	 */
	@Select("<script>"
			+ "select s.id as id, s.script_name as title, s.cover_url as coverUrl, s.episodes as episodes, "
			+ "  s.price_yuan as price, s.sale_status as saleStatus, s.topic as topic, "
			+ "  ca.nickname as authorName, f.setTime as favoritedAt "
			+ "from creator_script_favorite f join script s on s.id = f.script_id "
			+ "left join creator_account ca on ca.id = s.author_id "
			+ "where f.creator_id = #{creatorId} and ifnull(s.delete_state, 0) = 0 "
			+ "<if test='keyword != null and keyword != \"\"'> and s.script_name like concat('%', #{keyword}, '%') </if>"
			+ "<if test='topic != null and topic != \"\"'> and s.topic = #{topic} </if>"
			+ "order by f.setTime desc, f.id desc"
			+ "</script>")
	List<ScriptFavoriteItemRespEntity> findFavoriteList(@Param("creatorId") Integer creatorId,
			@Param("keyword") String keyword, @Param("topic") String topic);
}
