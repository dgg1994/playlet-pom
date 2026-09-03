package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletTransfetContactsEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 内部转账通讯录 DAO。
 */
@Repository
public interface WalletTransfetContactsDao extends BaseMapper<WalletTransfetContactsEntity> {

	@Select("select * from wallet_transfet_contacts where wallet_uid = #{walletUid} "
			+ "and contacts_wallet_uid = #{contactsWalletUid} limit 1")
	WalletTransfetContactsEntity findOne(@Param("walletUid") Long walletUid,
			@Param("contactsWalletUid") Long contactsWalletUid);

	@Select("<script>"
			+ "select * from wallet_transfet_contacts where 1=1 "
			+ "<if test='walletUid != null'> and wallet_uid = #{walletUid}</if>"
			+ "<if test='contactsWalletUid != null'> and contacts_wallet_uid = #{contactsWalletUid}</if>"
			+ "<if test='contactsLabel != null'> and contacts_label like concat('%', #{contactsLabel}, '%')</if>"
			+ " order by setTime desc"
			+ "</script>")
	List<WalletTransfetContactsEntity> findList(WalletTransfetContactsEntity entity);
}
