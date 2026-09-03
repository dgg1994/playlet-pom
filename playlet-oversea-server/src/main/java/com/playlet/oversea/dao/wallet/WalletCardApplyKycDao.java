package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardApplyKycEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 开卡申请 KYC 证件快照。
 */
@Repository
public interface WalletCardApplyKycDao extends BaseMapper<WalletCardApplyKycEntity> {

	@Select("select * from wallet_card_apply_kyc where apply_id = #{applyId} limit 1")
	WalletCardApplyKycEntity findByApplyId(@Param("applyId") Long applyId);

	@Select("select * from wallet_card_apply_kyc where wallet_user_id = #{walletUserId} "
			+ "order by id desc limit 1")
	WalletCardApplyKycEntity findLatestByWalletUserId(@Param("walletUserId") Long walletUserId);
}
