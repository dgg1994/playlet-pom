package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletUserHolderEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包持卡人。
 */
@Repository
public interface WalletUserHolderDao extends BaseMapper<WalletUserHolderEntity> {

	@Select("select * from wallet_user_holder where wallet_user_id = #{walletUserId} order by id desc")
	List<WalletUserHolderEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_user_holder where id = #{id} and wallet_user_id = #{walletUserId} limit 1")
	WalletUserHolderEntity findOwned(@Param("id") Long id, @Param("walletUserId") Long walletUserId);
}
