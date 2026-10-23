package com.playlet.internal.dao.member;

import com.playlet.internal.api.request.RechargeOrderQuery;
import com.playlet.internal.api.response.RechargeOrderItemEntity;
import com.playlet.internal.api.response.RechargeOrderSummaryEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 充值订单统一查询 Dao（会员单 + 单集解锁单）。
 */
@Repository
public interface RechargeOrderDao {

	@Select("<script>"
			+ "SELECT * FROM ("
			+ "  SELECT mo.order_no AS orderNo, mo.uid AS uid, a.nickname AS nickname,"
			+ "    'MEMBER' AS productType,"
			+ "    CONCAT(IFNULL(mp.product_name, mo.product_code),"
			+ "      IF(mo.duration_days IS NULL, '', CONCAT(' · ', mo.duration_days, '天'))) AS productName,"
			+ "    mo.pay_amount AS payAmount, mo.pay_coin AS payCoin,"
			+ "    IF(mo.pay_type = 'COIN', 'COIN', IFNULL(mo.pay_channel, '')) AS payChannel,"
			+ "    mo.pay_status AS payStatus, mo.discount_amt AS discountAmt,"
			+ "    mo.user_coupon_id AS userCouponId, mo.third_trade_no AS thirdTradeNo,"
			+ "    mo.pay_time AS payTime, mo.setTime AS setTime, mo.duration_days AS durationDays,"
			+ "    CAST(NULL AS SIGNED) AS dramaId, CAST(NULL AS SIGNED) AS assetId"
			+ "  FROM member_order mo"
			+ "  LEFT JOIN app_account a ON a.id = mo.uid"
			+ "  LEFT JOIN member_product mp ON mp.id = mo.product_id"
			+ "  UNION ALL"
			+ "  SELECT eo.order_no AS orderNo, eo.uid AS uid, a2.nickname AS nickname,"
			+ "    'EPISODE' AS productType,"
			+ "    CONCAT('单集解锁 · ', IFNULL(d.drama_title, CONCAT('#', eo.asset_id))) AS productName,"
			+ "    eo.pay_amount AS payAmount, 0 AS payCoin,"
			+ "    IFNULL(eo.pay_channel, '') AS payChannel,"
			+ "    eo.pay_status AS payStatus, eo.discount_amt AS discountAmt,"
			+ "    eo.user_coupon_id AS userCouponId, eo.third_trade_no AS thirdTradeNo,"
			+ "    eo.pay_time AS payTime, eo.setTime AS setTime,"
			+ "    CAST(NULL AS SIGNED) AS durationDays, eo.drama_id AS dramaId, eo.asset_id AS assetId"
			+ "  FROM episode_unlock_order eo"
			+ "  LEFT JOIN app_account a2 ON a2.id = eo.uid"
			+ "  LEFT JOIN drama d ON d.id = eo.drama_id"
			+ ") u WHERE 1=1"
			+ "<if test='keyword != null and keyword != \"\"'>"
			+ "  AND (u.orderNo = #{keyword}"
			+ "    OR CAST(u.uid AS CHAR) = #{keyword}"
			+ "    OR u.nickname LIKE CONCAT('%', #{keyword}, '%'))"
			+ "</if>"
			+ "<if test='productType != null and productType != \"\"'> AND u.productType = #{productType} </if>"
			+ "<if test='payChannel != null and payChannel != \"\"'> AND u.payChannel = #{payChannel} </if>"
			+ "<if test='payStatus != null'> AND u.payStatus = #{payStatus} </if>"
			+ " ORDER BY u.setTime DESC"
			+ "</script>")
	List<RechargeOrderItemEntity> findAdminList(RechargeOrderQuery query);

	@Select("<script>"
			+ "SELECT * FROM ("
			+ "  SELECT mo.order_no AS orderNo, mo.uid AS uid, a.nickname AS nickname,"
			+ "    'MEMBER' AS productType,"
			+ "    CONCAT(IFNULL(mp.product_name, mo.product_code),"
			+ "      IF(mo.duration_days IS NULL, '', CONCAT(' · ', mo.duration_days, '天'))) AS productName,"
			+ "    mo.pay_amount AS payAmount, mo.pay_coin AS payCoin,"
			+ "    IF(mo.pay_type = 'COIN', 'COIN', IFNULL(mo.pay_channel, '')) AS payChannel,"
			+ "    mo.pay_status AS payStatus, mo.discount_amt AS discountAmt,"
			+ "    mo.user_coupon_id AS userCouponId, mo.third_trade_no AS thirdTradeNo,"
			+ "    mo.pay_time AS payTime, mo.setTime AS setTime, mo.duration_days AS durationDays,"
			+ "    CAST(NULL AS SIGNED) AS dramaId, CAST(NULL AS SIGNED) AS assetId"
			+ "  FROM member_order mo"
			+ "  LEFT JOIN app_account a ON a.id = mo.uid"
			+ "  LEFT JOIN member_product mp ON mp.id = mo.product_id"
			+ "  UNION ALL"
			+ "  SELECT eo.order_no AS orderNo, eo.uid AS uid, a2.nickname AS nickname,"
			+ "    'EPISODE' AS productType,"
			+ "    CONCAT('单集解锁 · ', IFNULL(d.drama_title, CONCAT('#', eo.asset_id))) AS productName,"
			+ "    eo.pay_amount AS payAmount, 0 AS payCoin,"
			+ "    IFNULL(eo.pay_channel, '') AS payChannel,"
			+ "    eo.pay_status AS payStatus, eo.discount_amt AS discountAmt,"
			+ "    eo.user_coupon_id AS userCouponId, eo.third_trade_no AS thirdTradeNo,"
			+ "    eo.pay_time AS payTime, eo.setTime AS setTime,"
			+ "    CAST(NULL AS SIGNED) AS durationDays, eo.drama_id AS dramaId, eo.asset_id AS assetId"
			+ "  FROM episode_unlock_order eo"
			+ "  LEFT JOIN app_account a2 ON a2.id = eo.uid"
			+ "  LEFT JOIN drama d ON d.id = eo.drama_id"
			+ ") u WHERE u.orderNo = #{orderNo}"
			+ "<if test='productType != null and productType != \"\"'> AND u.productType = #{productType} </if>"
			+ " LIMIT 1"
			+ "</script>")
	RechargeOrderItemEntity findDetail(@Param("orderNo") String orderNo,
			@Param("productType") String productType);

	@Select("SELECT"
			+ "  IFNULL((SELECT SUM(x.pay_amount) FROM ("
			+ "    SELECT pay_amount, pay_status, setTime FROM member_order"
			+ "    UNION ALL"
			+ "    SELECT pay_amount, pay_status, setTime FROM episode_unlock_order"
			+ "  ) x WHERE x.pay_status = 1 AND x.setTime >= CURDATE()), 0) AS todayAmount,"
			+ "  IFNULL((SELECT COUNT(1) FROM ("
			+ "    SELECT setTime FROM member_order"
			+ "    UNION ALL"
			+ "    SELECT setTime FROM episode_unlock_order"
			+ "  ) c WHERE c.setTime >= CURDATE()), 0) AS todayOrderCount,"
			+ "  IFNULL((SELECT"
			+ "    CASE WHEN (paid_cnt + refund_cnt) = 0 THEN 0"
			+ "      ELSE ROUND(refund_cnt * 100.0 / (paid_cnt + refund_cnt), 1) END"
			+ "    FROM ("
			+ "      SELECT"
			+ "        SUM(CASE WHEN pay_status = 1 THEN 1 ELSE 0 END) AS paid_cnt,"
			+ "        SUM(CASE WHEN pay_status = 3 THEN 1 ELSE 0 END) AS refund_cnt"
			+ "      FROM ("
			+ "        SELECT pay_status, setTime FROM member_order"
			+ "        UNION ALL"
			+ "        SELECT pay_status, setTime FROM episode_unlock_order"
			+ "      ) t WHERE t.setTime >= CURDATE()"
			+ "    ) r"
			+ "  ), 0) AS refundRate,"
			+ "  IFNULL((SELECT SUM(y.pay_amount) FROM ("
			+ "    SELECT pay_amount, pay_status FROM member_order"
			+ "    UNION ALL"
			+ "    SELECT pay_amount, pay_status FROM episode_unlock_order"
			+ "  ) y WHERE y.pay_status = 1), 0) AS pendingSettleAmount")
	RechargeOrderSummaryEntity summary();
}
