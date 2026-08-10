package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaEntity;
import com.playlet.internal.query.drama.QueryDramaQuery;
import com.playlet.internal.query.drama.RecommendDramaQuery;
import com.playlet.internal.response.drama.RecommendDramaRes;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaDao extends BaseMapper<DramaEntity> {

	/** C端：仅已上架未删除（id 为主键；verify_status=1 对应 VerifyStateEnums.AVAILABLE_NOW） */
	@Select("select * from drama where id = #{dramaId} and verify_status = 1 and delete_state = 0 limit 1")
	DramaEntity findOnlineByDramaId(@Param("dramaId") Integer dramaId);

	/** 管理端：按主键查（含草稿，不含软删） */
	@Select("select * from drama where id = #{dramaId} and delete_state = 0 limit 1")
	DramaEntity findByDramaId(@Param("dramaId") Integer dramaId);

	/** 批量：未软删（对齐 findByDramaId） */
	@Select("<script>"
			+ "select * from drama where delete_state = 0 and id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaEntity> findByIds(@Param("ids") List<Integer> ids);

	/** 批量：含软删（对齐 interact 消息里 findByDramaId 失败后 selectById 回退） */
	@Select("<script>"
			+ "select * from drama where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaEntity> findByIdsIncludeDeleted(@Param("ids") List<Integer> ids);

	@Select("<script>"
			+ "select * from drama where delete_state = 0 "
			+ "<if test='verifyStatus != null'> and verify_status = #{verifyStatus} </if>"
			+ "<if test='dramaTitle != null and dramaTitle != \"\"'> and drama_title like concat('%',#{dramaTitle},'%') </if>"
			+ "<if test='id != null'> and id = #{id} </if>"
			+ "<if test='producerFirm != null and producerFirm != \"\"'> and producer_firm like concat('%',#{producerFirm},'%') </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaEntity> findAdminList(DramaEntity entity);

	/** C端剧场搜索：标题模糊 + 标签分组精确，仅已上架；条件可单独或组合 */
	@Select("<script>"
			+ "select distinct d.* from drama d "
			+ "where d.verify_status = 1 and ifnull(d.delete_state, 0) = 0 "
			+ "<if test='entity.dramaTitle != null and entity.dramaTitle != \"\"'> "
			+ "  and d.drama_title like concat('%', #{entity.dramaTitle}, '%') "
			+ "</if>"
			+ "<if test='entity.tagGroupId != null and entity.tagGroupId != \"\"'> "
			+ "  and exists ( "
			+ "    select 1 from drama_tag_rel r "
			+ "    where r.drama_id = d.id and r.tag_group_id = #{entity.tagGroupId} "
			+ "  ) "
			+ "</if>"
			+ "order by ifnull(d.hot_score, 0) desc, d.id desc"
			+ "</script>")
	List<DramaEntity> searchOnline(@Param("entity") DramaEntity entity);

	@Select("<script>"
	        + "select * from drama where 1=1"
	        + "<if test='dramaTitle != null and dramaTitle != \"\"'>"
	        + " and ("
	        + "   drama_title like concat('%', #{dramaTitle}, '%')"
	        + "   or exists ("
	        + "     select 1"
	        + "     from drama_tag_rel dtr"
	        + "     inner join dic_drama_tag dt on dt.group_id = dtr.tag_group_id"
	        + "     where dtr.drama_id = drama.id"
	        + "       and dt.tag_name like concat('%', #{dramaTitle}, '%')"
	        + "   )"
	        + " )"
	        + "</if>"
	        + "<if test='finishedState != null'> and finished_state = #{finishedState}</if>"
	        + "<if test='videoType != null'> and video_type = #{videoType}</if>"
	        + "<if test='producerFirm != null'> and producer_firm = #{producerFirm}</if>"
	        + "<if test='deleteState != null'> and delete_state = #{deleteState}</if>"
	        + "<if test='verifyStatus != null'> and verify_status = #{verifyStatus}</if>"
	        + "<if test='recommendedCarousel != null'> and recommended_carousel = #{recommendedCarousel}</if>"
			+ "<if test='groupId != null'>"
			+ " and id in ("
			+ "   select drama_id from drama_tag_rel where tag_group_id = #{groupId}"
			+ " )"
			+ "</if>"
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

	@Update("update drama set hot_score = ifnull(hot_score,0) + #{delta}, gmtModified = now() where id = #{dramaId}")
	int incrHotScore(@Param("dramaId") Integer dramaId, @Param("delta") long delta);

	@Select("SELECT * FROM drama WHERE delete_state = #{deleteState} AND verify_status = #{verifyStatus} "
			+ "ORDER BY CRC32(CONCAT(id, #{seed})), id")
	List<RecommendDramaRes> recommendList(RecommendDramaQuery entity);

	@Select("SELECT * from drama where id = (SELECT drama_id from drama_asset where id = #{id})")
	DramaEntity findByVideoId(@Param("id") Integer id);

	@Update("update drama set share_score = ifnull(share_score,0) + 1, gmtModified = now() where id = #{dramaId}")
	void incrShareScore(@Param("dramaId") Integer dramaId);
	@Select("SELECT d.* "
	        + "FROM drama d "
	        + "WHERE d.delete_state = #{deleteState} "
	        + "  AND d.verify_status = #{verifyStatus} "
	        + "  AND d.id != #{id} "
	        + "  AND EXISTS ("
	        + "      SELECT 1 "
	        + "      FROM drama_tag_rel dtr "
	        + "      WHERE dtr.drama_id = d.id "
	        + "        AND dtr.tag_group_id IN ( "
	        + "            SELECT tag_group_id "
	        + "            FROM drama_tag_rel "
	        + "            WHERE drama_id = #{id} "
	        + "        )"
	        + "  ) "
	        + "LIMIT 20")
	List<RecommendDramaRes> relatedWork(@Param("id") Integer id,@Param("deleteState") Integer deleteState,@Param("verifyStatus") Integer verifyStatus);

	@Select("select * from drama where id = #{id}")
	RecommendDramaRes findById(@Param("id") Integer id);

	@Select("select d.* from drama d "
			+ "inner join rank_list rl on d.id = rl.drama_id "
			+ "where rl.board_group_id = #{groupId} and rl.status = 1 "
			+ "and d.verify_status = #{verifyStatus} and ifnull(d.delete_state, 0) = #{deleteState} "
			+ "order by rl.rank_no asc")
	List<DramaEntity> selectListDramas(@Param("groupId") String groupId,
			@Param("verifyStatus") Integer verifyStatus,
			@Param("deleteState") Integer deleteState);
}
