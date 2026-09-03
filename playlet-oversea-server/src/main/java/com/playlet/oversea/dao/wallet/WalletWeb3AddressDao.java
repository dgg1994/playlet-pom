package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletWeb3AddressEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * Web3 充值地址 DAO。
 */
@Repository
public interface WalletWeb3AddressDao extends BaseMapper<WalletWeb3AddressEntity> {

	@Select("select * from wallet_web3_address where wallet_uid = #{walletUid} limit 1")
	WalletWeb3AddressEntity findByWalletUid(@Param("walletUid") Long walletUid);

	@Select("select * from wallet_web3_address where tron_address = #{address} or bnb_address = #{address} "
			+ "or eth_address = #{address} or btc_address = #{address} limit 1")
	WalletWeb3AddressEntity findByAnyAddress(@Param("address") String address);
}
