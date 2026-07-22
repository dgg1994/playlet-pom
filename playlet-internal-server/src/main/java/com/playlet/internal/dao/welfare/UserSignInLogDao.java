package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.UserSignInLogEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSignInLogDao extends BaseMapper<UserSignInLogEntity> {

	@Select("select * from user_sign_in_log where uid = #{uid} and biz_date = #{bizDate} limit 1")
	UserSignInLogEntity findOne(@Param("uid") String uid, @Param("bizDate") String bizDate);

	@Select("select * from user_sign_in_log where uid = #{uid} and biz_date >= #{startDate} "
			+ "and biz_date <= #{endDate} order by biz_date asc")
	List<UserSignInLogEntity> findByUidAndDateRange(@Param("uid") String uid,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	@Select("select count(1) from user_sign_in_log where uid = #{uid} and sign_type = #{signType} "
			+ "and biz_date >= #{startDate} and biz_date <= #{endDate}")
	int countByUidAndTypeAndDateRange(@Param("uid") String uid, @Param("signType") Integer signType,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	@Select("select biz_date from user_sign_in_log where uid = #{uid} order by biz_date desc")
	List<String> findBizDatesByUid(@Param("uid") String uid);
}
