package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaTagRelEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaTagRelDao extends BaseMapper<DramaTagRelEntity> {

	@Select("select dtr.* from drama_tag_rel dtr left join drama_tag dt on dtr.tag_id = dt.id where dt.tag_name = #{tagName}")
	List<DramaTagRelEntity>  selectByTagName(@Param("tagName") String tagName);
}
