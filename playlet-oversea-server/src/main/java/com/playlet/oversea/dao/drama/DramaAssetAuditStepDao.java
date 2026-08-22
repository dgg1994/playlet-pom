package com.playlet.oversea.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.drama.DramaAssetAuditStepEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaAssetAuditStepDao extends BaseMapper<DramaAssetAuditStepEntity> {

	@Select("select * from drama_asset_audit_step where asset_id = #{assetId} order by step_type asc")
	List<DramaAssetAuditStepEntity> findByAssetId(@Param("assetId") Integer assetId);

	@Select("select * from drama_asset_audit_step where asset_id = #{assetId} and step_type = #{stepType} limit 1")
	DramaAssetAuditStepEntity findByAssetIdAndStepType(@Param("assetId") Integer assetId,
			@Param("stepType") Integer stepType);

	@Select("<script>"
			+ "select * from drama_asset_audit_step where asset_id in "
			+ "<foreach collection='assetIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ " order by asset_id asc, step_type asc"
			+ "</script>")
	List<DramaAssetAuditStepEntity> findByAssetIds(@Param("assetIds") List<Integer> assetIds);

	@Delete("delete from drama_asset_audit_step where asset_id = #{assetId}")
	void deleteByAssetId(@Param("assetId") Integer assetId);
}
