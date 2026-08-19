package com.playlet.internal.dao.creator;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.creator.CreatorCoinLedgerEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

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

	@Select("select * from creator_coin_ledger where creator_id = #{creatorId} and biz_type = #{bizType} "
			+ "and biz_id = #{bizId} limit 1")
	CreatorCoinLedgerEntity findByBiz(@Param("creatorId") Integer creatorId, @Param("bizType") String bizType,
			@Param("bizId") String bizId);
}
