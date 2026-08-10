package com.playlet.oversea.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.welfare.WithdrawConfigEntity;
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
}
