package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaAssetDao extends BaseMapper<DramaAssetEntity> {

	@Select("select * from drama_asset where drama_id = #{dramaId} and asset_type = #{assetType} "
			+ "and status = 1 and deleted = 0 limit 1")
	DramaAssetEntity findByDramaAndType(@Param("dramaId") String dramaId, @Param("assetType") String assetType);

	@Select("<script>"
			+ "select * from drama_asset where deleted = 0 "
			+ "<if test='dramaId != null and dramaId != \"\"'> and drama_id = #{dramaId} </if>"
			+ "<if test='assetType != null and assetType != \"\"'> and asset_type = #{assetType} </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaAssetEntity> findAdminList(DramaAssetEntity entity);
}
