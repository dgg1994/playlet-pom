package com.playlet.oversea.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.api.response.CreatorRevenueTrendAggRow;
import com.playlet.oversea.entity.creator.CreatorCoinLedgerEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 作家金币流水。
 */
@Repository
public interface CreatorCoinLedgerDao extends BaseMapper<CreatorCoinLedgerEntity> {

	/** 自然日正入账合计（今日/昨日收益） */
	@Select("select ifnull(sum(change_amt), 0) from creator_coin_ledger "
			+ "where creator_id = #{creatorId} and change_amt > 0 "
			+ "and date(setTime) = #{bizDate}")
	Long sumPositiveIncomeByDate(@Param("creatorId") Integer creatorId, @Param("bizDate") String bizDate);

	/** 日期区间内正入账按自然日聚合（趋势图）；SELECT/GROUP BY/ORDER BY 表达式须一致以兼容 only_full_group_by */
	@Select("select date_format(setTime, '%Y-%m-%d') as bizDate, ifnull(sum(change_amt), 0) as incomeCoin "
			+ "from creator_coin_ledger where creator_id = #{creatorId} and change_amt > 0 "
			+ "and date(setTime) >= #{fromDate} and date(setTime) <= #{toDate} "
			+ "group by date_format(setTime, '%Y-%m-%d') order by date_format(setTime, '%Y-%m-%d')")
	List<CreatorRevenueTrendAggRow> sumPositiveIncomeGroupByDate(@Param("creatorId") Integer creatorId,
			@Param("fromDate") String fromDate, @Param("toDate") String toDate);

	@Select("select * from creator_coin_ledger where creator_id = #{creatorId} and biz_type = #{bizType} "
			+ "and biz_id = #{bizId} limit 1")
	CreatorCoinLedgerEntity findByBiz(@Param("creatorId") Integer creatorId, @Param("bizType") String bizType,
			@Param("bizId") String bizId);

	/** 作家资金流水（按时间倒序） */
	@Select("select * from creator_coin_ledger where creator_id = #{creatorId} "
			+ "order by setTime desc, id desc")
	List<CreatorCoinLedgerEntity> findByCreatorId(@Param("creatorId") Integer creatorId);
}
