package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WelfareTaskI18nEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareTaskI18nDao extends BaseMapper<WelfareTaskI18nEntity> {

	@Select("select * from welfare_task_i18n where task_code = #{taskCode} and langue = #{langue} limit 1")
	WelfareTaskI18nEntity findByTaskCodeAndLangue(@Param("taskCode") String taskCode,
			@Param("langue") String langue);

	@Select("select * from welfare_task_i18n where task_code = #{taskCode}")
	List<WelfareTaskI18nEntity> findByTaskCode(@Param("taskCode") String taskCode);

	@Select("<script>"
			+ "select * from welfare_task_i18n where langue = #{langue} and task_code in "
			+ "<foreach collection='taskCodes' item='code' open='(' separator=',' close=')'>#{code}</foreach>"
			+ "</script>")
	List<WelfareTaskI18nEntity> findByLangueAndTaskCodes(@Param("langue") String langue,
			@Param("taskCodes") List<String> taskCodes);
}
