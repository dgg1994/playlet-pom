package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.TagEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagDao extends BaseMapper<TagEntity> {

	@Select("select * from drama_tag where tag_name = #{tagName} limit 1")
	TagEntity findByTagName(@Param("tagName") String tagName);


	@Select("<script>"
			+ "select * from drama_tag where 1=1 "
			+ "<if test='tagName != null and tagName != \"\"'> and tag_name like concat('%',#{tagName},'%') </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by sort_weight desc, id desc"
			+ "</script>")
	List<TagEntity> findAdminList(TagEntity entity);

	@Select("select t.* from drama_tag t "
			+ "inner join drama_tag_rel r on t.id = r.tag_id "
			+ "where r.drama_id = #{dramaId} and t.status = 1 "
			+ "order by r.sort_weight desc, t.sort_weight desc, t.id desc")
	List<TagEntity> findByDramaId(@Param("dramaId") String dramaId);
}
