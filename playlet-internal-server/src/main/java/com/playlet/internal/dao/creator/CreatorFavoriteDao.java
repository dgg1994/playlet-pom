package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.CreatorFavoriteItemResp;
import com.playlet.internal.entity.creator.CreatorFavoriteEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 创作者收藏。
 */
@Repository
public interface CreatorFavoriteDao extends BaseMapper<CreatorFavoriteEntity> {

	/** 收藏（重复收藏忽略） */
	@Insert("insert ignore into creator_favorite(uid, creator_id, setTime, gmtModified) "
			+ "values(#{uid}, #{creatorId}, now(), now())")
	int insertIgnore(@Param("uid") Integer uid, @Param("creatorId") Integer creatorId);

	/** 取消收藏 */
	@Delete("delete from creator_favorite where uid = #{uid} and creator_id = #{creatorId}")
	int deleteByUidAndCreator(@Param("uid") Integer uid, @Param("creatorId") Integer creatorId);

	/** 是否已收藏 */
	@Select("select count(1) from creator_favorite where uid = #{uid} and creator_id = #{creatorId}")
	int countByUidAndCreator(@Param("uid") Integer uid, @Param("creatorId") Integer creatorId);

	/**
	 * 收藏列表：创作者基础信息 + 已通过认证的身份/擅长领域 + 代表作名称聚合。
	 */
	@Select("<script>"
			+ "select ca.id as creatorId, ca.nickname as name, ca.avatar as avatarUrl, "
			+ " (select c.identity_types from creator_certification c where c.creator_id = ca.id and c.audit_status = 2 order by c.category limit 1) as identityTypes, "
			+ " (select c.category from creator_certification c where c.creator_id = ca.id and c.audit_status = 2 order by c.category limit 1) as identityCategory, "
			+ " (select c.`field` from creator_certification c where c.creator_id = ca.id and c.audit_status = 2 order by c.category limit 1) as `field`, "
			+ " (select group_concat(r.script_name separator ' / ') from creator_representative r where r.creator_id = ca.id) as works, "
			+ " f.setTime as favoritedAt "
			+ "from creator_favorite f join creator_account ca on ca.id = f.creator_id "
			+ "where f.uid = #{uid} "
			+ "<if test='keyword != null and keyword != \"\"'> and ca.nickname like concat('%', #{keyword}, '%') </if>"
			+ "order by f.setTime desc, f.id desc"
			+ "</script>")
	List<CreatorFavoriteItemResp> findFavoriteList(@Param("uid") Integer uid, @Param("keyword") String keyword);
}
