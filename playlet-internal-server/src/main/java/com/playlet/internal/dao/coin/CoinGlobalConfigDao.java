package com.playlet.internal.dao.coin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.coin.CoinGlobalConfigEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 金币全局配置 Dao。
 */
@Repository
public interface CoinGlobalConfigDao extends BaseMapper<CoinGlobalConfigEntity> {

	@Select("select * from coin_global_config order by id asc limit 1")
	CoinGlobalConfigEntity findOne();
}
