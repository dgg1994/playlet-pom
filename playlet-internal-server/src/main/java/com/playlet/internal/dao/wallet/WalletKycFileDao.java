package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletKycFileEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 KYC 证件文件。
 */
@Repository
public interface WalletKycFileDao extends BaseMapper<WalletKycFileEntity> {

	@Select("select * from wallet_kyc_file where wallet_user_id = #{walletUserId} order by id desc")
	List<WalletKycFileEntity> findByWalletUserId(@Param("walletUserId") Long walletUserId);

	@Select("select * from wallet_kyc_file where wallet_user_id = #{walletUserId} "
			+ "and document_type = #{documentType} order by id desc limit 1")
	WalletKycFileEntity findLatestByType(@Param("walletUserId") Long walletUserId,
			@Param("documentType") Integer documentType);
}
