package com.playlet.internal.dao.ops;

import com.playlet.internal.api.response.OpsDashboardHotDramaItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 运营看板聚合查询（跨表）。
 */
@Repository
public interface OpsStatDao {

	/** 区间新增用户（按注册 setTime） */
	@Select("select ifnull(count(1), 0) from app_account "
			+ "where setTime >= #{startTime} and setTime < #{endTimeExclusive}")
	long countNewUsers(@Param("startTime") String startTime, @Param("endTimeExclusive") String endTimeExclusive);

	/** 区间总播放秒：剧维度日表（与榜单同源，可作兜底） */
	@Select("select ifnull(sum(valid_seconds), 0) from drama_rank_stat_daily "
			+ "where biz_date >= #{startDate} and biz_date <= #{endDate}")
	long sumDramaPlaySeconds(@Param("startDate") String startDate, @Param("endDate") String endDate);

	/** 现金充值合计：会员现金单 + 单集解锁单（已支付） */
	@Select("SELECT IFNULL(SUM(x.amt), 0) FROM ("
			+ "  SELECT pay_amount AS amt FROM member_order "
			+ "    WHERE pay_status = 1 AND IFNULL(pay_type,'CASH') <> 'COIN' "
			+ "      AND setTime >= #{startTime} AND setTime < #{endTimeExclusive} "
			+ "  UNION ALL "
			+ "  SELECT pay_amount AS amt FROM episode_unlock_order "
			+ "    WHERE pay_status = 1 "
			+ "      AND setTime >= #{startTime} AND setTime < #{endTimeExclusive} "
			+ ") x")
	BigDecimal sumCashRecharge(@Param("startTime") String startTime,
			@Param("endTimeExclusive") String endTimeExclusive);

	/** 会员现金收入 */
	@Select("SELECT IFNULL(SUM(pay_amount), 0) FROM member_order "
			+ "WHERE pay_status = 1 AND IFNULL(pay_type,'CASH') <> 'COIN' "
			+ "  AND setTime >= #{startTime} AND setTime < #{endTimeExclusive}")
	BigDecimal sumMemberCash(@Param("startTime") String startTime,
			@Param("endTimeExclusive") String endTimeExclusive);

	/** 单集付费收入 */
	@Select("SELECT IFNULL(SUM(pay_amount), 0) FROM episode_unlock_order "
			+ "WHERE pay_status = 1 "
			+ "  AND setTime >= #{startTime} AND setTime < #{endTimeExclusive}")
	BigDecimal sumEpisodeCash(@Param("startTime") String startTime,
			@Param("endTimeExclusive") String endTimeExclusive);

	/** 金币消费折合（元）：解锁表 COIN 的 cost_yuan */
	@Select("SELECT IFNULL(SUM(IFNULL(cost_yuan, 0)), 0) FROM user_episode_unlock "
			+ "WHERE unlock_way = 'COIN' "
			+ "  AND setTime >= #{startTime} AND setTime < #{endTimeExclusive}")
	BigDecimal sumCoinSpendYuan(@Param("startTime") String startTime,
			@Param("endTimeExclusive") String endTimeExclusive);

	/** 付费用户数（去重 uid） */
	@Select("SELECT COUNT(DISTINCT uid) FROM ("
			+ "  SELECT uid FROM member_order "
			+ "    WHERE pay_status = 1 AND setTime >= #{startTime} AND setTime < #{endTimeExclusive} "
			+ "  UNION "
			+ "  SELECT uid FROM episode_unlock_order "
			+ "    WHERE pay_status = 1 AND setTime >= #{startTime} AND setTime < #{endTimeExclusive} "
			+ ") t")
	long countPayingUsers(@Param("startTime") String startTime,
			@Param("endTimeExclusive") String endTimeExclusive);

	/** 在架短剧数 */
	@Select("SELECT COUNT(1) FROM drama "
			+ "WHERE IFNULL(shelf_status, 0) = 1 AND (delete_state = 0 OR delete_state IS NULL)")
	long countOnShelfDrama();

	/** 待审核短剧（待审/审核中/申诉中） */
	@Select("SELECT COUNT(1) FROM drama "
			+ "WHERE (delete_state = 0 OR delete_state IS NULL) "
			+ "  AND IFNULL(audit_status, 0) IN (0, 1, 4)")
	long countPendingAuditDrama();

	/** 待处理退款申请数 */
	@Select("SELECT COUNT(1) FROM refund_apply WHERE status = 0")
	long countPendingRefundApply();

	/** 已支付未退款订单笔数（待结算近似） */
	@Select("SELECT COUNT(1) FROM ("
			+ "  SELECT id FROM member_order WHERE pay_status = 1 "
			+ "  UNION ALL "
			+ "  SELECT id FROM episode_unlock_order WHERE pay_status = 1 "
			+ ") t")
	long countPendingSettleOrders();

	/** 已支付未退款金额合计（待结算近似） */
	@Select("SELECT IFNULL(SUM(x.amt), 0) FROM ("
			+ "  SELECT pay_amount AS amt FROM member_order WHERE pay_status = 1 "
			+ "  UNION ALL "
			+ "  SELECT pay_amount AS amt FROM episode_unlock_order WHERE pay_status = 1 "
			+ ") x")
	BigDecimal sumPendingSettleAmount();

	/** 按日现金充值 */
	@Select("SELECT DATE_FORMAT(x.setTime, '%Y-%m-%d') AS bizDate, IFNULL(SUM(x.amt), 0) AS amount "
			+ "FROM ("
			+ "  SELECT setTime, pay_amount AS amt FROM member_order "
			+ "    WHERE pay_status = 1 AND IFNULL(pay_type,'CASH') <> 'COIN' "
			+ "      AND setTime >= #{startTime} AND setTime < #{endTimeExclusive} "
			+ "  UNION ALL "
			+ "  SELECT setTime, pay_amount AS amt FROM episode_unlock_order "
			+ "    WHERE pay_status = 1 "
			+ "      AND setTime >= #{startTime} AND setTime < #{endTimeExclusive} "
			+ ") x "
			+ "GROUP BY DATE_FORMAT(x.setTime, '%Y-%m-%d')")
	List<Map<String, Object>> sumCashRechargeByDay(@Param("startTime") String startTime,
			@Param("endTimeExclusive") String endTimeExclusive);

	/** 按日播放次数（play_pv） */
	@Select("SELECT biz_date AS bizDate, IFNULL(SUM(play_pv), 0) AS playCount "
			+ "FROM drama_rank_stat_daily "
			+ "WHERE biz_date >= #{startDate} AND biz_date <= #{endDate} "
			+ "GROUP BY biz_date")
	List<Map<String, Object>> sumPlayPvByDay(@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	/** 热播 TOP：区间播放 PV 倒序（摘要用，带 limit） */
	@Select("SELECT d.id AS dramaId, d.drama_title AS dramaName, "
			+ "  IFNULL(( "
			+ "    SELECT GROUP_CONCAT(DISTINCT t.tag_name ORDER BY t.id SEPARATOR '、') "
			+ "    FROM drama_tag_rel r "
			+ "    INNER JOIN dic_drama_tag t ON t.group_id = r.tag_group_id "
			+ "    WHERE r.drama_id = d.id "
			+ "  ), '') AS dramaTag, "
			+ "  IFNULL(SUM(s.play_pv), 0) AS playCount, "
			+ "  IFNULL(( "
			+ "    SELECT SUM(eo.pay_amount) FROM episode_unlock_order eo "
			+ "    WHERE eo.pay_status = 1 AND eo.drama_id = d.id "
			+ "      AND eo.setTime >= #{todayStart} AND eo.setTime < #{todayEndExclusive} "
			+ "  ), 0) AS todayRecharge "
			+ "FROM drama_rank_stat_daily s "
			+ "INNER JOIN drama d ON d.id = s.drama_id "
			+ "WHERE s.biz_date >= #{startDate} AND s.biz_date <= #{endDate} "
			+ "  AND (d.delete_state = 0 OR d.delete_state IS NULL) "
			+ "GROUP BY d.id, d.drama_title "
			+ "ORDER BY playCount DESC, d.id DESC "
			+ "LIMIT #{limit}")
	List<OpsDashboardHotDramaItem> selectHotDramaTop(@Param("startDate") String startDate,
			@Param("endDate") String endDate,
			@Param("todayStart") String todayStart,
			@Param("todayEndExclusive") String todayEndExclusive,
			@Param("limit") int limit);

	/** 热播排行分页（无 LIMIT，配合 PageHelper） */
	@Select("SELECT d.id AS dramaId, d.drama_title AS dramaName, "
			+ "  IFNULL(( "
			+ "    SELECT GROUP_CONCAT(DISTINCT t.tag_name ORDER BY t.id SEPARATOR '、') "
			+ "    FROM drama_tag_rel r "
			+ "    INNER JOIN dic_drama_tag t ON t.group_id = r.tag_group_id "
			+ "    WHERE r.drama_id = d.id "
			+ "  ), '') AS dramaTag, "
			+ "  IFNULL(SUM(s.play_pv), 0) AS playCount, "
			+ "  IFNULL(( "
			+ "    SELECT SUM(eo.pay_amount) FROM episode_unlock_order eo "
			+ "    WHERE eo.pay_status = 1 AND eo.drama_id = d.id "
			+ "      AND eo.setTime >= #{todayStart} AND eo.setTime < #{todayEndExclusive} "
			+ "  ), 0) AS todayRecharge "
			+ "FROM drama_rank_stat_daily s "
			+ "INNER JOIN drama d ON d.id = s.drama_id "
			+ "WHERE s.biz_date >= #{startDate} AND s.biz_date <= #{endDate} "
			+ "  AND (d.delete_state = 0 OR d.delete_state IS NULL) "
			+ "GROUP BY d.id, d.drama_title "
			+ "ORDER BY playCount DESC, d.id DESC")
	List<OpsDashboardHotDramaItem> selectHotDramaPage(@Param("startDate") String startDate,
			@Param("endDate") String endDate,
			@Param("todayStart") String todayStart,
			@Param("todayEndExclusive") String todayEndExclusive);
}
