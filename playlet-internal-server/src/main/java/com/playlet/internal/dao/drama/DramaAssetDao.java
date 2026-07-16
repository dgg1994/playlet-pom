package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaAssetEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DramaAssetDao extends BaseMapper<DramaAssetEntity> {

	/** 按剧取一条可用资源（未删除且 video_status=1） */
	@Select("select * from drama_asset where drama_id = #{dramaId} "
			+ "and video_status = 1 and delete_state = 0 order by id desc limit 1")
	DramaAssetEntity findEnabledByDramaId(@Param("dramaId") Integer dramaId);

	@Select("<script>"
			+ "select * from drama_asset where delete_state = 0 "
			+ "<if test='dramaId != null'> and drama_id = #{dramaId} </if>"
			+ "<if test='videoStatus != null'> and video_status = #{videoStatus} </if>"
			+ "order by id desc"
			+ "</script>")
	List<DramaAssetEntity> findAdminList(DramaAssetEntity entity);
}
