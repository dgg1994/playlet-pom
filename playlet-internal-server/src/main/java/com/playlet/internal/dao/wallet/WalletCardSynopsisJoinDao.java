package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardSynopsisJoinEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * U 卡产品与简介关联 DAO。
 */
@Repository
public interface WalletCardSynopsisJoinDao extends BaseMapper<WalletCardSynopsisJoinEntity> {

	@Delete("delete from wallet_card_synopsis_join where card_id = #{cardId}")
	int deleteByCardId(@Param("cardId") String cardId);

	@Delete("delete from wallet_card_synopsis_join where synopsis_id = #{synopsisId}")
	int deleteBySynopsisId(@Param("synopsisId") Integer synopsisId);
}
