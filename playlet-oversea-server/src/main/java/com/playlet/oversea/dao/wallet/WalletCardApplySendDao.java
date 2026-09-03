package com.playlet.oversea.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.wallet.WalletCardApplySendEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 开卡申请邮寄地址。
 */
@Repository
public interface WalletCardApplySendDao extends BaseMapper<WalletCardApplySendEntity> {

	@Select("select * from wallet_card_apply_send where apply_id = #{applyId} limit 1")
	WalletCardApplySendEntity findByApplyId(@Param("applyId") Long applyId);
}
