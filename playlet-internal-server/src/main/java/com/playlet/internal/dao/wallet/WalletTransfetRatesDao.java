package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletTransfetRatesEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 内部转账费率 DAO。
 */
@Repository
public interface WalletTransfetRatesDao extends BaseMapper<WalletTransfetRatesEntity> {

	@Select("select * from wallet_transfet_rates order by id asc limit 1")
	WalletTransfetRatesEntity findFirst();
}
