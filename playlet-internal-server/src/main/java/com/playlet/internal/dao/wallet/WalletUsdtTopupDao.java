package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletUsdtTopupEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * USDT 链上充值流水。
 */
@Repository
public interface WalletUsdtTopupDao extends BaseMapper<WalletUsdtTopupEntity> {

	@Select("select * from wallet_usdt_topup_log where tx_hash = #{txHash} limit 1")
	WalletUsdtTopupEntity findByTxHash(@Param("txHash") String txHash);
}
