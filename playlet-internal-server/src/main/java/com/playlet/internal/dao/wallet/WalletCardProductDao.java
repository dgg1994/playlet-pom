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

	@Select("<script>"
			+ "select * from wallet_card_product where enable = 1 "
			+ "<if test='bankcardNature != null and bankcardNature != \"\"'>"
			+ " and bankcard_nature = #{bankcardNature} "
			+ "</if>"
			+ "order by hot desc, id asc"
			+ "</script>")
	List<WalletCardProductEntity> findEnabledList(@Param("bankcardNature") String bankcardNature);

	@Select("<script>"
			+ "select * from wallet_card_product where 1 = 1 "
			+ "<if test='enable != null'> and enable = #{enable} </if>"
			+ "<if test='cardTitle != null and cardTitle != \"\"'> and card_title like concat('%', #{cardTitle}, '%') </if>"
			+ "order by hot desc, id asc"
			+ "</script>")
	List<WalletCardProductEntity> findAdminList(WalletCardProductEntity entity);

	@Select("select * from wallet_card_product where id = #{id} limit 1")
	WalletCardProductEntity findById(@Param("id") Integer id);

	@Select("<script>select id, card_img from wallet_card_product where id in "
			+ "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<WalletCardProductEntity> findCardImgByIds(@Param("ids") List<Integer> ids);
}
