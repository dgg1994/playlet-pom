package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardShippingEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 实体卡发货记录。
 */
@Repository
public interface WalletCardShippingDao extends BaseMapper<WalletCardShippingEntity> {

	@Select("select * from wallet_card_shipping where apply_id = #{applyId} limit 1")
	WalletCardShippingEntity findByApplyId(@Param("applyId") Long applyId);
}
