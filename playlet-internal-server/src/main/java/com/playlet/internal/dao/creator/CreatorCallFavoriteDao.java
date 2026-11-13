package com.playlet.internal.dao.creator;

import com.playlet.internal.api.response.CallFavoriteItemRespEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 征稿收藏 DAO。
 */
@Mapper
public interface CreatorCallFavoriteDao {

	/** 幂等插入（忽略重复键） */
	@Insert("insert ignore into creator_call_favorite(creator_id, call_id) values(#{creatorId}, #{callId})")
	int insertIgnore(@Param("creatorId") Integer creatorId, @Param("callId") Integer callId);

	/** 取消收藏 */
	@Delete("delete from creator_call_favorite where creator_id = #{creatorId} and call_id = #{callId}")
	int deleteByCreatorAndCall(@Param("creatorId") Integer creatorId, @Param("callId") Integer callId);

	/** 是否已收藏 */
	@Select("select count(1) from creator_call_favorite where creator_id = #{creatorId} and call_id = #{callId}")
	int countByCreatorAndCall(@Param("creatorId") Integer creatorId, @Param("callId") Integer callId);

	/** 当前创作者收藏的所有征稿 id */
	@Select("select call_id from creator_call_favorite where creator_id = #{creatorId}")
	List<Integer> findFavoriteCallIds(@Param("creatorId") Integer creatorId);

	/**
	 * 我的征稿收藏列表。
	 * 关键字匹配标题，题材精确匹配。只返回仍在架上、未删除的征稿。
	 * deadline 返回原始 Date，文案由 Service 层后处理。
	 */
	@Select("<script>"
			+ "select cc.id as id, cc.title as title, cc.topic as topic, cc.amount as amount, "
			+ "  cc.exclusive as exclusive, cc.need_count as need, cc.participants as participants, "
			+ "  cc.publisher as publisher, cc.deadline as deadline, f.setTime as favoritedAt "
			+ "from creator_call_favorite f join creator_call cc on cc.id = f.call_id "
			+ "where f.creator_id = #{creatorId} and cc.shelf_status = 1 and cc.delete_state = 0 "
			+ "<if test='keyword != null and keyword != \"\"'> and cc.title like concat('%', #{keyword}, '%') </if>"
			+ "<if test='topic != null and topic != \"\"'> and cc.topic = #{topic} </if>"
			+ "order by f.setTime desc, f.id desc"
			+ "</script>")
	List<CallFavoriteItemRespEntity> findFavoriteList(@Param("creatorId") Integer creatorId,
			@Param("keyword") String keyword, @Param("topic") String topic);
}
