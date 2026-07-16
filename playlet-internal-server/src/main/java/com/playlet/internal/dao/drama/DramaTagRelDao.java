package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaTagRelEntity;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaTagRelDao extends BaseMapper<DramaTagRelEntity> {

	@Select("select dtr.* from drama_tag_rel dtr "
			+ "left join dic_drama_tag dt on dtr.tag_group_id = dt.group_id "
			+ "where dt.id = #{tagId}")
	List<DramaTagRelEntity> selectByTagId(@Param("tagId") Integer tagId);

	@Select("select * from drama_tag_rel where drama_id = #{dramaId}")
	List<DramaTagRelEntity> findByDramaId(@Param("dramaId") Integer dramaId);

	@Delete("delete from drama_tag_rel where drama_id = #{dramaId}")
	int deleteByDramaId(@Param("dramaId") Integer dramaId);
}
