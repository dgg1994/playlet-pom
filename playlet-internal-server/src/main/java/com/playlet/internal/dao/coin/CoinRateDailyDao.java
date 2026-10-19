package com.playlet.internal.dao.coin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.coin.CoinRateDailyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 金币日汇率 Dao。
 */
@Repository
public interface CoinRateDailyDao extends BaseMapper<CoinRateDailyEntity> {

	@Select("select * from coin_rate_daily where biz_date = #{bizDate} limit 1")
	CoinRateDailyEntity findByBizDate(@Param("bizDate") String bizDate);

	@Select("select * from coin_rate_daily order by biz_date desc limit 1")
	CoinRateDailyEntity findLatest();
}
