package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.api.response.DramaAssetRespEntity;
import com.playlet.internal.api.response.RecommendVidoeRespEntity;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaAssetDao extends BaseMapper<DramaAssetEntity> {

	/** 按剧取一条可用资源（未删除且 video_status=1） */
	@Select("select * from drama_asset where drama_id = #{dramaId} "
			+ "and video_status = 1 and shelf_status = 1 and delete_state = 0 order by id desc limit 1")
	DramaAssetEntity findEnabledByDramaId(@Param("dramaId") Integer dramaId);

	@Select("<script>"
			+ "select * from drama_asset where delete_state = 0 "
			+ "<if test='dramaId != null'> and drama_id = #{dramaId} </if>"
			+ "<if test='videoStatus != null'> and video_status = #{videoStatus} </if>"
			+ "<if test='auditStatus != null'> and audit_status = #{auditStatus} </if>"
			+ "<if test='shelfStatus != null'> and shelf_status = #{shelfStatus} </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaAssetEntity> findAdminList(DramaAssetEntity entity);

	@Select("select * from drama_asset where drama_id = #{dramaId} and set_num = #{setNum} "
			+ "and ifnull(delete_state, 0) = 0 order by id desc limit 1")
	DramaAssetEntity findByDramaIdAndSetNum(@Param("dramaId") Integer dramaId, @Param("setNum") Integer setNum);

	@Update("update drama_asset set delete_state = #{deleteState} where drama_id = #{dramaId}")
	void updateDramaIdDeleteState(@Param("dramaId") Integer dramaId,@Param("deleteState") Integer deleteState);

	@Select("select ifnull(count(*),0) from drama_asset where drama_id = #{dramaId}")
	Integer findByDramaIdNum(@Param("dramaId") Integer dramaId);

	/** 已上架集数量（未删除） */
	@Select("select ifnull(count(*),0) from drama_asset where drama_id = #{dramaId} "
			+ "and ifnull(delete_state, 0) = 0 and shelf_status = 1")
	Integer countOnShelfByDramaId(@Param("dramaId") Integer dramaId);

	/** 整剧下架时批量下架集 */
	@Update("update drama_asset set shelf_status = 0, video_status = 0, shelf_time = null, gmtModified = now() "
			+ "where drama_id = #{dramaId} and ifnull(delete_state, 0) = 0 and shelf_status = 1")
	int unshelfAllByDramaId(@Param("dramaId") Integer dramaId);

	/** 已过审集数量（未删除） */
	@Select("select ifnull(count(*),0) from drama_asset where drama_id = #{dramaId} "
			+ "and ifnull(delete_state, 0) = 0 and audit_status = 2")
	Integer countApprovedByDramaId(@Param("dramaId") Integer dramaId);

	/** 管理端整剧上架：批量上架该剧下已过审、未删除且当前未上架的集 */
	@Update("update drama_asset set shelf_status = 1, video_status = 1, "
			+ "shelf_time = ifnull(shelf_time, now()), gmtModified = now() "
			+ "where drama_id = #{dramaId} and ifnull(delete_state, 0) = 0 "
			+ "and audit_status = 2 and ifnull(shelf_status, 0) = 0")
	int shelfApprovedByDramaId(@Param("dramaId") Integer dramaId);

	@Select("select * from drama_asset where drama_id = #{dramaId} and delete_state = 0 order by set_num")
	List<DramaAssetRespEntity> findByDramaId(@Param("dramaId") Integer dramaId);

	/** 作家详情：该剧全部未删除集（含审核中/驳回，不限上架） */
	@Select("select * from drama_asset where drama_id = #{dramaId} and ifnull(delete_state, 0) = 0 "
			+ "order by set_num, id")
	List<DramaAssetEntity> findNotDeletedByDramaId(@Param("dramaId") Integer dramaId);

	@Select("SELECT * from drama_asset where drama_id = #{dramaId} and delete_state = #{deleteState} "
			+ "and video_status = 1 and shelf_status = 1 order by set_num limit 1")
	RecommendVidoeRespEntity findDramaIdOne(@Param("dramaId") Integer dramaId,@Param("deleteState") Integer deleteState);

	/** 批量取每部剧 set_num 最小的一集（推荐流装配，避免 N+1） */
	@Select("<script>"
			+ "SELECT a.* FROM drama_asset a "
			+ "INNER JOIN ("
			+ "  SELECT drama_id, MIN(set_num) AS min_set FROM drama_asset "
			+ "  WHERE delete_state = #{deleteState} AND drama_id IN "
			+ "  <foreach collection='dramaIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "  GROUP BY drama_id"
			+ ") t ON a.drama_id = t.drama_id AND a.set_num = t.min_set AND a.delete_state = #{deleteState}"
			+ "</script>")
	List<DramaAssetEntity> findFirstAssetsByDramaIds(@Param("dramaIds") List<Integer> dramaIds,
			@Param("deleteState") Integer deleteState);

	@Update("update drama_asset set like_score = ifnull(like_score,0) + 1, gmtModified = now() where id = #{assetId}")
	int incrLikeScore(@Param("assetId") Integer assetId);

	@Update("update drama_asset set like_score = greatest(ifnull(like_score,0) - 1, 0), gmtModified = now() where id = #{assetId}")
	int decrLikeScore(@Param("assetId") Integer assetId);

	/** 写入七牛 avinfo 解析出的时长 */
	@Update("update drama_asset set duration_seconds = #{durationSeconds}, gmtModified = now() where id = #{id}")
	int updateDurationSeconds(@Param("id") Integer id, @Param("durationSeconds") Integer durationSeconds);

	/** 曝光量 +1（观看上报去重后调用） */
	@Update("update drama_asset set exposure_count = ifnull(exposure_count,0) + 1, gmtModified = now() where id = #{id}")
	int incrExposureCount(@Param("id") Integer id);

	/** 完播量 +1（进度达阈值且去重后调用） */
	@Update("update drama_asset set complete_count = ifnull(complete_count,0) + 1, gmtModified = now() where id = #{id}")
	int incrCompleteCount(@Param("id") Integer id);

	@Select("select video_url from drama_asset where id = #{id}")
	String findVideoUrl(@Param("id") Integer id);

	@Select("<script>"
			+ "select id, video_url as videoUrl from drama_asset where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaAssetEntity> findIdAndVideoUrlByIds(@Param("ids") List<Integer> ids);

	@Select("select set_num from drama_asset where id = #{episodeId}")
	String selectSetNum(@Param("episodeId") String episodeId);

	@Select("<script>"
			+ "select id, set_num as setNum from drama_asset where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<DramaAssetEntity> findIdAndSetNumByIds(@Param("ids") List<String> ids);

}
