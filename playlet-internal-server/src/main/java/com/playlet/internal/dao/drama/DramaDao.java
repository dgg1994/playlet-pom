package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.query.drama.QueryDramaQuery;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaDao extends BaseMapper<DramaEntity> {

	/** C端：仅已上架未删除（id 为主键） */
	@Select("select * from drama where id = #{dramaId} and verify_status = 2 and delete_state = 0 limit 1")
	DramaEntity findOnlineByDramaId(@Param("dramaId") Integer dramaId);

	/** 管理端：按主键查（含草稿，不含软删） */
	@Select("select * from drama where id = #{dramaId} and delete_state = 0 limit 1")
	DramaEntity findByDramaId(@Param("dramaId") Integer dramaId);

	@Select("<script>"
			+ "select * from drama where delete_state = 0 "
			+ "<if test='verifyStatus != null'> and verify_status = #{verifyStatus} </if>"
			+ "<if test='dramaTitle != null and dramaTitle != \"\"'> and drama_title like concat('%',#{dramaTitle},'%') </if>"
			+ "<if test='id != null'> and id = #{id} </if>"
			+ "<if test='producerFirm != null and producerFirm != \"\"'> and producer_firm like concat('%',#{producerFirm},'%') </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaEntity> findAdminList(DramaEntity entity);

	/** C端剧场搜索：标题/简介/标签名，仅已上架 */
	@Select("select distinct d.* from drama d "
			+ "where d.verify_status = 2 and d.delete_state = 0 "
			+ "and ( "
			+ "  d.drama_title like concat('%', #{keyword}, '%') "
			+ "  or ifnull(d.description_info, '') like concat('%', #{keyword}, '%') "
			+ "  or exists ( "
			+ "    select 1 from drama_tag_rel r "
			+ "    inner join dic_drama_tag t on t.id = r.tag_id and t.status = 1 "
			+ "    where r.drama_id = d.id and t.tag_name like concat('%', #{keyword}, '%') "
			+ "  ) "
			+ ") "
			+ "order by d.hot_score desc, d.id desc")
	List<DramaEntity> searchOnline(@Param("keyword") String keyword);

	@Select("<script>"
	        + "select * from drama where 1=1"
	        + "<if test='dramaTitle != null'> and drama_title like CONCAT('%', #{dramaTitle}, '%')</if>"
	        + "<if test='finishedState != null'> and finished_state = #{finishedState}</if>"
	        + "<if test='videoType != null'> and video_type = #{videoType}</if>"
	        + "<if test='belongUser != null'> and belong_user = #{belongUser}</if>"
	        + "<if test='deleteState != null'> and delete_state = #{deleteState}</if>"
	        + "<if test='verifyStatus != null'> and verify_status = #{verifyStatus}</if>"
	        + "<if test='tagGroupIdList != null and tagGroupIdList.size() > 0'>"
	        + " and id in ("
	        + "   select drama_id from drama_tag_rel where tag_group_id in "
	        + "   <foreach collection='tagGroupIdList' item='tagId' open='(' separator=',' close=')'>"
	        + "     #{tagId}"
	        + "   </foreach>"
	        + " )"
	        + "</if>"
	        + "order by setTime desc"
	        + "</script>")
	List<DramaEntity> findList(QueryDramaQuery entity);

	@Update("update drama set collect_score = ifnull(collect_score,0) + 1, gmtModified = now() where id = #{dramaId}")
	int incrCollectScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set collect_score = greatest(ifnull(collect_score,0) - 1, 0), gmtModified = now() where id = #{dramaId}")
	int decrCollectScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set like_score = ifnull(like_score,0) + 1, gmtModified = now() where id = #{dramaId}")
	int incrLikeScore(@Param("dramaId") Integer dramaId);

	@Update("update drama set like_score = greatest(ifnull(like_score,0) - 1, 0), gmtModified = now() where id = #{dramaId}")
	int decrLikeScore(@Param("dramaId") Integer dramaId);
}
