package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WithdrawConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawConfigDao extends BaseMapper<WithdrawConfigEntity> {

	@Select("select * from withdraw_config where status = 1 order by sort_weight desc, id asc")
	List<WithdrawConfigEntity> findActiveList();

	@Select("select * from withdraw_config where status = 1 and asset_code = #{assetCode} "
			+ "and network = #{network} limit 1")
	WithdrawConfigEntity findActive(@Param("assetCode") String assetCode, @Param("network") String network);

	/** 管理端列表：按资产/网络/状态筛选 */
	@Select("<script>"
			+ "select * from withdraw_config where 1 = 1 "
			+ "<if test='assetCode != null and assetCode != \"\"'> and asset_code = #{assetCode} </if>"
			+ "<if test='network != null and network != \"\"'> and network = #{network} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "order by sort_weight desc, id desc"
			+ "</script>")
	List<WithdrawConfigEntity> findAdminList(WithdrawConfigEntity entity);

	/** 资产+网络唯一性校验（排除自身） */
	@Select("<script>"
			+ "select * from withdraw_config where asset_code = #{assetCode} and network = #{network} "
			+ "<if test='excludeId != null'> and id != #{excludeId} </if>"
			+ "limit 1"
			+ "</script>")
	WithdrawConfigEntity findByAssetAndNetwork(@Param("assetCode") String assetCode,
			@Param("network") String network,
			@Param("excludeId") Integer excludeId);
}
