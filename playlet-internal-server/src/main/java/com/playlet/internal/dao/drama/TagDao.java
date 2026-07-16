package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.TagEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagDao extends BaseMapper<TagEntity> {

	@Select("select * from dic_drama_tag where tag_name = #{tagName} limit 1")
	TagEntity findByTagName(@Param("tagName") String tagName);

	@Select("<script>"
			+ "select * from dic_drama_tag where 1=1 "
			+ "<if test='tagName != null and tagName != \"\"'> and tag_name like concat('%',#{tagName},'%') </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='langue != null and langue != \"\"'> and langue = #{langue} </if>"
			+ "<if test='groupId != null and groupId != \"\"'> and group_id = #{groupId} </if>"
			+ "order by sort_weight desc, id desc"
			+ "</script>")
	List<TagEntity> findAdminList(TagEntity entity);

	@Select("select t.* from dic_drama_tag t "
			+ "inner join drama_tag_rel r on t.id = r.tag_id "
			+ "where r.drama_id = #{dramaId} and t.status = 1 "
			+ "order by t.sort_weight desc, t.id desc")
	List<TagEntity> findByDramaId(@Param("dramaId") Integer dramaId);
	
	@Select("select * from dic_drama_tag where langue = #{language} and group_id in "
			+ "(select tag_group_id from drama_tag_rel where drama_id = #{dramaId})")
	List<TagEntity> findGroupLang(@Param("language") String language,@Param("dramaId") Integer dramaId);

	@Update("update dic_drama_tag set status = #{status},gmtModified = NOW() where group_id = #{groupId}")
	void updateStatusByGroupId(@Param("status") Integer status, @Param("groupId") String groupId);

	@Select("<script>"
			+ "select * from dic_drama_tag where status = 1 "
			+ "<if test='tagName != null and tagName != \"\"'> and tag_name like concat('%',#{tagName},'%') </if>"
			+ "<if test='langue != null and langue != \"\"'> and langue = #{langue} </if>"
			+ "order by sort_weight desc, id desc"
			+ "</script>")
	List<TagEntity> findAppList(TagEntity entity);
}
