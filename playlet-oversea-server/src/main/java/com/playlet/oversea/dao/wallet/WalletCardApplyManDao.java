package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardApplyManEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 开卡申请持卡人快照。
 */
@Repository
public interface WalletCardApplyManDao extends BaseMapper<WalletCardApplyManEntity> {

	@Select("select * from wallet_card_apply_man where apply_id = #{applyId} limit 1")
	WalletCardApplyManEntity findByApplyId(@Param("applyId") Long applyId);
}
