package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.UserCoinLedgerEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCoinLedgerDao extends BaseMapper<UserCoinLedgerEntity> {

	@Select("select * from user_coin_ledger where uid = #{uid} and biz_type = #{bizType} "
			+ "and biz_id = #{bizId} limit 1")
	UserCoinLedgerEntity findByBiz(@Param("uid") String uid, @Param("bizType") String bizType,
			@Param("bizId") String bizId);

	@Select("select * from user_coin_ledger where uid = #{uid} order by setTime desc, id desc")
	List<UserCoinLedgerEntity> findByUid(@Param("uid") String uid);

	@Select("<script>"
			+ "select * from user_coin_ledger where uid = #{uid} "
			+ "<if test='bizType != null and bizType != \"\"'> and biz_type = #{bizType} </if>"
			+ "order by setTime desc, id desc"
			+ "</script>")
	List<UserCoinLedgerEntity> findByUidAndBizType(@Param("uid") String uid, @Param("bizType") String bizType);
}
