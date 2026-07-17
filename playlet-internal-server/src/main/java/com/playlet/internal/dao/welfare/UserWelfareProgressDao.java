package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.UserWelfareProgressEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserWelfareProgressDao extends BaseMapper<UserWelfareProgressEntity> {

	@Select("select * from user_welfare_progress where uid = #{uid} and task_code = #{taskCode} "
			+ "and biz_date = #{bizDate} limit 1")
	UserWelfareProgressEntity findOne(@Param("uid") String uid, @Param("taskCode") String taskCode,
			@Param("bizDate") String bizDate);

	@Select("select * from user_welfare_progress where uid = #{uid} order by gmtModified desc")
	List<UserWelfareProgressEntity> findByUid(@Param("uid") String uid);

	@Select("<script>"
			+ "select * from user_welfare_progress where uid = #{uid} and task_code in "
			+ "<foreach collection='taskCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>"
			+ "</script>")
	List<UserWelfareProgressEntity> findByUidAndTaskCodes(@Param("uid") String uid,
			@Param("taskCodes") List<String> taskCodes);

	@Update("update user_welfare_progress set progress = #{progress}, progress_status = #{progressStatus}, "
			+ "gmtModified = now() where id = #{id}")
	int updateProgress(@Param("id") Long id, @Param("progress") Integer progress,
			@Param("progressStatus") Integer progressStatus);

	@Update("update user_welfare_progress set progress_status = #{progressStatus}, claim_time = #{claimTime}, "
			+ "gmtModified = now() where id = #{id}")
	int updateClaim(@Param("id") Long id, @Param("progressStatus") Integer progressStatus,
			@Param("claimTime") Date claimTime);
}
