package com.playlet.internal.dao.ad;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.ad.AdSlotConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 广告位配置 Dao。
 */
@Repository
public interface AdSlotConfigDao extends BaseMapper<AdSlotConfigEntity> {

	@Select("select * from ad_slot_config where slot_code = #{slotCode} limit 1")
	AdSlotConfigEntity findBySlotCode(@Param("slotCode") String slotCode);

	@Select("select * from ad_slot_config where status = 1 order by id asc")
	List<AdSlotConfigEntity> findEnabledList();

	@Select("<script>"
			+ "select * from ad_slot_config where 1=1 "
			+ "<if test='slotCode != null and slotCode != \"\"'> and slot_code = #{slotCode} </if>"
			+ "<if test='platform != null and platform != \"\"'> and platform = #{platform} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by id desc"
			+ "</script>")
	List<AdSlotConfigEntity> findAdminList(AdSlotConfigEntity entity);
}
