package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletTollAddressEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收款地址 DAO。
 */
@Repository
public interface WalletTollAddressDao extends BaseMapper<WalletTollAddressEntity> {

	@Select("<script>"
			+ "select * from wallet_toll_address where 1=1 "
			+ "<if test='addressType != null and addressType != \"\"'> and address_type = #{addressType} </if>"
			+ "<if test='addressSite != null and addressSite != \"\"'> and address_site like concat('%', #{addressSite}, '%') </if>"
			+ " order by id desc"
			+ "</script>")
	List<WalletTollAddressEntity> findList(WalletTollAddressEntity entity);

	@Select("select * from wallet_toll_address where address_type = #{addressType} limit 1")
	WalletTollAddressEntity findByType(String addressType);
}
