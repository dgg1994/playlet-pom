package com.playlet.internal.dao.member;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.member.UserPayAgreementEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 代扣签约协议 Dao。
 */
@Repository
public interface UserPayAgreementDao extends BaseMapper<UserPayAgreementEntity> {

	@Select("select * from user_pay_agreement where external_agreement_no = #{externalAgreementNo} limit 1")
	UserPayAgreementEntity findByExternalAgreementNo(@Param("externalAgreementNo") String externalAgreementNo);

	@Select("select * from user_pay_agreement where uid = #{uid} and status = 1 "
			+ "order by id desc limit 1")
	UserPayAgreementEntity findSignedLatestByUid(@Param("uid") Integer uid);

	@Select("select * from user_pay_agreement where uid = #{uid} order by id desc")
	List<UserPayAgreementEntity> findByUid(@Param("uid") Integer uid);

	/** 待扣款：已签约且到了下次扣款时间 */
	@Select("select * from user_pay_agreement where status = 1 and next_deduct_time is not null "
			+ "and next_deduct_time <= #{now} order by next_deduct_time asc limit #{limit}")
	List<UserPayAgreementEntity> findDueRenewals(@Param("now") Date now, @Param("limit") int limit);
}
