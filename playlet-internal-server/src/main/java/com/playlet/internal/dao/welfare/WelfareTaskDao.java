package com.playlet.internal.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.welfare.WelfareTaskEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WelfareTaskDao extends BaseMapper<WelfareTaskEntity> {


	@Select("<script>"
			+ "select * from welfare_task where 1=1 "
			+ "<if test='status != null'> and status = #{status} </if>"
			+ "<if test='cycleType != null'> and cycle_type = #{cycleType} </if>"
			+ "order by sort_weight desc, id asc"
			+ "</script>")
	List<WelfareTaskEntity> findAdminList(WelfareTaskEntity entity);
}
