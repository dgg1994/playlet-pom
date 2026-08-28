package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.WithdrawOrderAdminItemEntity;
import com.playlet.internal.entity.welfare.UserWithdrawOrderEntity;
import com.playlet.internal.query.welfare.WithdrawOrderAdminQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWithdrawOrderDao extends BaseMapper<UserWithdrawOrderEntity> {

	@Select("select * from user_withdraw_order where order_no = #{orderNo} limit 1")
	UserWithdrawOrderEntity findByOrderNo(@Param("orderNo") String orderNo);

	@Select("select * from user_withdraw_order where request_order_id = #{requestOrderId} limit 1")
	UserWithdrawOrderEntity findByRequestOrderId(@Param("requestOrderId") String requestOrderId);

	@Select("select * from user_withdraw_order where uid = #{uid} and user_type = #{userType} "
			+ "order by setTime desc, id desc")
	List<UserWithdrawOrderEntity> findByUid(@Param("uid") Integer uid, @Param("userType") Integer userType);

	@Select("select * from user_withdraw_order where uid = #{uid} and user_type = #{userType} "
			+ "order by setTime desc, id desc limit 1")
	UserWithdrawOrderEntity findLatestByUid(@Param("uid") Integer uid, @Param("userType") Integer userType);

	@Select("select * from user_withdraw_order where uid = #{uid} and user_type = #{userType} "
			+ "and asset_code = #{assetCode} and network = #{network} "
			+ "order by setTime desc, id desc limit 1")
	UserWithdrawOrderEntity findLatestByUidAndAsset(@Param("uid") Integer uid, @Param("userType") Integer userType,
			@Param("assetCode") String assetCode, @Param("network") String network);

	@Select("select ifnull(sum(points_amt),0) from user_withdraw_order "
			+ "where uid = #{uid} and user_type = #{userType} and setTime >= #{dayStart} "
			+ "and status in (0,1,2)")
	Integer sumPointsToday(@Param("uid") Integer uid, @Param("userType") Integer userType,
			@Param("dayStart") String dayStart);

	@Select("select ifnull(sum(points_amt),0) from user_withdraw_order "
			+ "where uid = #{uid} and user_type = #{userType} and asset_code = #{assetCode} "
			+ "and network = #{network} and setTime >= #{dayStart} and status in (0,1,2)")
	Integer sumPointsTodayByAsset(@Param("uid") Integer uid, @Param("userType") Integer userType,
			@Param("assetCode") String assetCode, @Param("network") String network,
			@Param("dayStart") String dayStart);

	@Update("update user_withdraw_order set status = #{toStatus}, gmtModified = now() "
			+ "where id = #{id} and status = #{fromStatus}")
	int casStatus(@Param("id") Long id, @Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus);

	@Update("update user_withdraw_order set status = #{toStatus}, third_order_no = #{thirdOrderNo}, "
			+ "fail_reason = #{failReason}, gmtModified = now() "
			+ "where id = #{id} and status = #{fromStatus}")
	int casFinish(@Param("id") Long id, @Param("fromStatus") Integer fromStatus,
			@Param("toStatus") Integer toStatus, @Param("thirdOrderNo") String thirdOrderNo,
			@Param("failReason") String failReason);

	/** 进行中提现单：待处理 + 打款中 */
	@Select("select ifnull(count(1),0) from user_withdraw_order "
			+ "where uid = #{uid} and user_type = #{userType} and status in (0, 1)")
	int countProcessingByUid(@Param("uid") Integer uid, @Param("userType") Integer userType);

	/**
	 * 管理端 C 端提现列表：关联 app_account 昵称。
	 */
	@Select("<script>"
			+ "select o.id, o.order_no as orderNo, o.uid, "
			+ "a.nickname as userName, o.points_amt as withdrawCoin, o.actual_amt as currencyAmt, "
			+ "wb.card_no as payAccount, o.asset_code as assetCode, o.network, "
			+ "o.status, o.setTime "
			+ "from user_withdraw_order o "
			+ "left join app_account a on a.id = o.uid "
			+ "left join wallet_bankcard wb on wb.id = o.target_bankcard_id "
			+ "where o.user_type = #{userType} "
			+ "<if test='q.orderNo != null and q.orderNo != \"\"'> and o.order_no = #{q.orderNo} </if>"
			+ "<if test='q.uid != null'> and o.uid = #{q.uid} </if>"
			+ "<if test='q.nickname != null and q.nickname != \"\"'> and a.nickname like concat('%', #{q.nickname}, '%') </if>"
			+ "<if test='q.assetCode != null and q.assetCode != \"\"'> and o.asset_code = #{q.assetCode} </if>"
			+ "<if test='q.network != null and q.network != \"\"'> and o.network = #{q.network} </if>"
			+ "<if test='q.status != null'> and o.status = #{q.status} </if>"
			+ "<if test='q.status == null and q.processFlag != null and q.processFlag == 0'> and o.status in (0, 1) </if>"
			+ "<if test='q.status == null and q.processFlag != null and q.processFlag == 1'> and o.status in (2, 3, 4) </if>"
			+ "<if test='q.startTime != null and q.startTime != \"\"'> and o.setTime &gt;= #{q.startTime} </if>"
			+ "<if test='q.endTime != null and q.endTime != \"\"'> and o.setTime &lt;= #{q.endTime} </if>"
			+ "order by o.setTime desc, o.id desc"
			+ "</script>")
	List<WithdrawOrderAdminItemEntity> findAdminAppList(@Param("q") WithdrawOrderAdminQuery q,
			@Param("userType") Integer userType);

	/**
	 * 管理端作家提现列表：关联 creator_account 昵称。
	 */
	@Select("<script>"
			+ "select o.id, o.order_no as orderNo, o.uid, "
			+ "c.nickname as userName, o.points_amt as withdrawCoin, o.actual_amt as currencyAmt, "
			+ "wb.card_no as payAccount, o.asset_code as assetCode, o.network, "
			+ "o.status, o.setTime "
			+ "from user_withdraw_order o "
			+ "left join creator_account c on c.id = o.uid "
			+ "left join wallet_bankcard wb on wb.id = o.target_bankcard_id "
			+ "where o.user_type = #{userType} "
			+ "<if test='q.orderNo != null and q.orderNo != \"\"'> and o.order_no = #{q.orderNo} </if>"
			+ "<if test='q.uid != null'> and o.uid = #{q.uid} </if>"
			+ "<if test='q.nickname != null and q.nickname != \"\"'> and c.nickname like concat('%', #{q.nickname}, '%') </if>"
			+ "<if test='q.assetCode != null and q.assetCode != \"\"'> and o.asset_code = #{q.assetCode} </if>"
			+ "<if test='q.network != null and q.network != \"\"'> and o.network = #{q.network} </if>"
			+ "<if test='q.status != null'> and o.status = #{q.status} </if>"
			+ "<if test='q.status == null and q.processFlag != null and q.processFlag == 0'> and o.status in (0, 1) </if>"
			+ "<if test='q.status == null and q.processFlag != null and q.processFlag == 1'> and o.status in (2, 3, 4) </if>"
			+ "<if test='q.startTime != null and q.startTime != \"\"'> and o.setTime &gt;= #{q.startTime} </if>"
			+ "<if test='q.endTime != null and q.endTime != \"\"'> and o.setTime &lt;= #{q.endTime} </if>"
			+ "order by o.setTime desc, o.id desc"
			+ "</script>")
	List<WithdrawOrderAdminItemEntity> findAdminCreatorList(@Param("q") WithdrawOrderAdminQuery q,
			@Param("userType") Integer userType);
}

