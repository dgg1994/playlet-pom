package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 钱包 U 卡产品缓存。
 */
@Repository
public interface WalletCardProductDao extends BaseMapper<WalletCardProductEntity> {

	@Select("select * from wallet_card_product where enable = 1 order by hot desc, id asc")
	List<WalletCardProductEntity> findEnabledList();

	@Select("select * from wallet_card_product where id = #{id} limit 1")
	WalletCardProductEntity findById(@Param("id") Integer id);
}
