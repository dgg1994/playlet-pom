package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaDao extends BaseMapper<DramaEntity> {

	/** C端：仅已上架未删除 */
	@Select("select * from drama where drama_id = #{dramaId} and status = 2 and deleted = 0 limit 1")
	DramaEntity findOnlineByDramaId(@Param("dramaId") String dramaId);

	/** 管理端：按业务ID查（含草稿，不含软删） */
	@Select("select * from drama where drama_id = #{dramaId} and deleted = 0 limit 1")
	DramaEntity findByDramaId(@Param("dramaId") String dramaId);

	@Select("<script>"
			+ "select * from drama where deleted = 0 "
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='title != null and title != \"\"'> and title like concat('%',#{title},'%') </if>"
			+ "<if test='dramaId != null and dramaId != \"\"'> and drama_id = #{dramaId} </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaEntity> findAdminList(DramaEntity entity);

	/** C端剧场搜索：标题/简介/标签名，仅已上架 */
	@Select("select distinct d.* from drama d "
			+ "where d.status = 2 and d.deleted = 0 "
			+ "and ( "
			+ "  d.title like concat('%', #{keyword}, '%') "
			+ "  or ifnull(d.description, '') like concat('%', #{keyword}, '%') "
			+ "  or exists ( "
			+ "    select 1 from drama_tag_rel r "
			+ "    inner join drama_tag t on t.id = r.tag_id and t.status = 1 "
			+ "    where r.drama_id = d.drama_id and t.tag_name like concat('%', #{keyword}, '%') "
			+ "  ) "
			+ ") "
			+ "order by d.hot_score desc, d.id desc")
	List<DramaEntity> searchOnline(@Param("keyword") String keyword);
}
