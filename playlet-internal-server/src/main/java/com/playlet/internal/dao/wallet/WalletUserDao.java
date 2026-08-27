package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletUserEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 钱包三方用户映射。
 */
@Repository
public interface WalletUserDao extends BaseMapper<WalletUserEntity> {

	@Select("select * from wallet_user where user_type = #{userType} and local_uid = #{localUid} limit 1")
	WalletUserEntity findByLocal(@Param("userType") Integer userType, @Param("localUid") Integer localUid);

	@Select("select * from wallet_user where wallet_uid = #{walletUid} limit 1")
	WalletUserEntity findByWalletUid(@Param("walletUid") Long walletUid);

	@Select("select * from wallet_user where email = #{email} and status = 1 limit 1")
	WalletUserEntity findByEmail(@Param("email") String email);
}
