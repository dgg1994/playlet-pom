package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WelfareTaskI18nEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareTaskI18nDao extends BaseMapper<WelfareTaskI18nEntity> {

	@Select("select * from welfare_task_i18n where task_id = #{taskId}")
	List<WelfareTaskI18nEntity> findByTaskCode(@Param("taskId") Integer taskId);

	@Delete("delete from welfare_task_i18n where task_id = #{taskId}")
	int deleteByTaskCode(@Param("taskId") Integer taskId);

	@Select("select task_name from welfare_task_i18n where task_id = #{taskId} and langue = #{language}")
	String selectNameById(@Param("taskId") Integer taskId, @Param("language") String language);

	@Select("select * from welfare_task_i18n where task_id = #{taskId} and langue = #{language} limit 1")
	WelfareTaskI18nEntity findByTaskIdAndLangue(@Param("taskId") Integer taskId,
			@Param("language") String language);

}
