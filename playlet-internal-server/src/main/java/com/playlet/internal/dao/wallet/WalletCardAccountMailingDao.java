package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardAccountMailingEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户收件地址 DAO。
 */
@Repository
public interface WalletCardAccountMailingDao extends BaseMapper<WalletCardAccountMailingEntity> {

	@Select("select * from wallet_card_account_mailing where wallet_user_id = #{walletUserId} "
			+ "and address_id = #{addressId} limit 1")
	WalletCardAccountMailingEntity findByWalletUserAndAddressId(@Param("walletUserId") Long walletUserId,
			@Param("addressId") Integer addressId);

	@Select("<script>"
			+ "select * from wallet_card_account_mailing where wallet_user_id = #{walletUserId}"
			+ "<if test='countryRegionId != null'> and country_region_id = #{countryRegionId}</if>"
			+ "<if test='country != null and country != \"\"'> and country = #{country}</if>"
			+ "<if test='receiverName != null and receiverName != \"\"'> and receiver_name = #{receiverName}</if>"
			+ " order by setTime desc, id desc"
			+ "</script>")
	List<WalletCardAccountMailingEntity> findList(WalletCardAccountMailingEntity query);
}
