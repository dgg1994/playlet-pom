package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import com.playlet.internal.response.drama.DramaAssetRes;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaAssetDao extends BaseMapper<DramaAssetEntity> {

	@Select("select * from drama_asset where drama_id = #{dramaId} and asset_type = #{assetType} "
			+ "and status = 1 and deleted = 0 limit 1")
	DramaAssetEntity findByDramaAndType(@Param("dramaId") Integer dramaId, @Param("assetType") String assetType);

	@Select("<script>"
			+ "select * from drama_asset where deleted = 0 "
			+ "<if test='dramaId != null and dramaId != \"\"'> and drama_id = #{dramaId} </if>"
			+ "<if test='assetType != null and assetType != \"\"'> and asset_type = #{assetType} </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaAssetEntity> findAdminList(DramaAssetEntity entity);

	@Update("update drama_asset set delete_state = #{deleteState} where drama_id = #{dramaId}")
	void updateDramaIdDeleteState(@Param("dramaId") Integer dramaId,@Param("deleteState") Integer deleteState);

	@Select("select ifnull(count(*),0) from drama_asset where drama_id = #{dramaId}")
	Integer findByDramaIdNum(@Param("dramaId") Integer dramaId);

	@Select("select * from drama_asset where drama_id = #{dramaId}")
	List<DramaAssetRes> findByDramaId(@Param("dramaId") Integer dramaId);
	
}
