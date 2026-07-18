package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WelfareTaskEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareTaskDao extends BaseMapper<WelfareTaskEntity> {

	@Select("select * from welfare_task where status = 1 order by sort_weight desc, id asc")
	List<WelfareTaskEntity> findEnabledList();

	@Select("select * from welfare_task where task_code = #{taskCode} limit 1")
	WelfareTaskEntity findByTaskCode(@Param("taskCode") String taskCode);

	@Select("<script>"
			+ "select * from welfare_task where 1=1 "
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='cycleType != null'> and cycle_type = #{cycleType} </if>"
			+ "<if test='taskCode != null and taskCode != \"\"'> and task_code = #{taskCode} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<WelfareTaskEntity> findAdminList(WelfareTaskEntity entity);
}
