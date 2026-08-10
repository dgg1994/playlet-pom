package com.playlet.oversea.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.welfare.UserWithdrawOrderEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWithdrawOrderDao extends BaseMapper<UserWithdrawOrderEntity> {

	@Select("select * from user_withdraw_order where order_no = #{orderNo} limit 1")
	UserWithdrawOrderEntity findByOrderNo(@Param("orderNo") String orderNo);

	@Select("select * from user_withdraw_order where uid = #{uid} order by setTime desc, id desc")
	List<UserWithdrawOrderEntity> findByUid(@Param("uid") Integer uid);

	@Select("select * from user_withdraw_order where uid = #{uid} order by setTime desc, id desc limit 1")
	UserWithdrawOrderEntity findLatestByUid(@Param("uid") Integer uid);

	@Select("select * from user_withdraw_order where uid = #{uid} and asset_code = #{assetCode} "
			+ "and network = #{network} order by setTime desc, id desc limit 1")
	UserWithdrawOrderEntity findLatestByUidAndAsset(@Param("uid") Integer uid,
			@Param("assetCode") String assetCode, @Param("network") String network);

	@Select("select ifnull(sum(points_amt),0) from user_withdraw_order "
			+ "where uid = #{uid} and setTime >= #{dayStart} "
			+ "and status in (0,1,2)")
	Integer sumPointsToday(@Param("uid") Integer uid, @Param("dayStart") String dayStart);

	@Select("select ifnull(sum(points_amt),0) from user_withdraw_order "
			+ "where uid = #{uid} and asset_code = #{assetCode} and network = #{network} "
			+ "and setTime >= #{dayStart} and status in (0,1,2)")
	Integer sumPointsTodayByAsset(@Param("uid") Integer uid, @Param("assetCode") String assetCode,
			@Param("network") String network, @Param("dayStart") String dayStart);

	@Update("update user_withdraw_order set status = #{toStatus}, gmtModified = now() "
			+ "where id = #{id} and status = #{fromStatus}")
	int casStatus(@Param("id") Long id, @Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus);

	@Update("update user_withdraw_order set status = #{toStatus}, tx_hash = #{txHash}, "
			+ "fail_reason = #{failReason}, gmtModified = now() "
			+ "where id = #{id} and status = #{fromStatus}")
	int casFinish(@Param("id") Long id, @Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus, @Param("txHash") String txHash,
			@Param("failReason") String failReason);
}
