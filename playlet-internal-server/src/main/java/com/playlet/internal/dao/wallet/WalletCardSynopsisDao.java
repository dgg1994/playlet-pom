package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletCardSynopsisEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * U 卡产品简介 DAO。
 */
@Repository
public interface WalletCardSynopsisDao extends BaseMapper<WalletCardSynopsisEntity> {

	@Select("<script>"
			+ "select * from wallet_card_synopsis where 1=1 "
			+ "<if test='language != null and language != \"\"'> and language = #{language} </if>"
			+ "<if test='title != null and title != \"\"'> and title like concat('%', #{title}, '%') </if>"
			+ " order by id desc"
			+ "</script>")
	List<WalletCardSynopsisEntity> findList(WalletCardSynopsisEntity entity);

	@Select("select s.* from wallet_card_synopsis s "
			+ "where s.id in (select synopsis_id from wallet_card_synopsis_join where card_id = #{cardId}) "
			+ "and s.language = #{language} limit 1")
	WalletCardSynopsisEntity findByCardId(@Param("cardId") String cardId, @Param("language") String language);

	@Select("select synopsis_id from wallet_card_synopsis_join where card_id = #{cardId}")
	List<Integer> querySynopsisIdsByCardId(@Param("cardId") String cardId);
}
