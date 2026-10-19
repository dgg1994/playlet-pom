package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.member.EpisodeUnlockOrderEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 单集现金解锁订单 Dao。
 */
@Repository
public interface EpisodeUnlockOrderDao extends BaseMapper<EpisodeUnlockOrderEntity> {

	@Select("select * from episode_unlock_order where order_no = #{orderNo} limit 1")
	EpisodeUnlockOrderEntity findByOrderNo(@Param("orderNo") String orderNo);

	@Select("select * from episode_unlock_order where third_trade_no = #{thirdTradeNo} limit 1")
	EpisodeUnlockOrderEntity findByThirdTradeNo(@Param("thirdTradeNo") String thirdTradeNo);

	@Select("<script>"
			+ "select * from episode_unlock_order where 1=1 "
			+ "<if test='uid != null'> and uid = #{uid} </if>"
			+ "<if test='assetId != null'> and asset_id = #{assetId} </if>"
			+ "<if test='payStatus != null'> and pay_status = #{payStatus} </if>"
			+ "<if test='orderNo != null and orderNo != \"\"'> and order_no = #{orderNo} </if>"
			+ "order by id desc"
			+ "</script>")
	List<EpisodeUnlockOrderEntity> findAdminList(EpisodeUnlockOrderEntity entity);
}
