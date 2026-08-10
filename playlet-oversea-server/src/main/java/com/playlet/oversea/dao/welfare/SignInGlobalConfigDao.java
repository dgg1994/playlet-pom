package com.playlet.oversea.dao.welfare;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.welfare.SignInGlobalConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface SignInGlobalConfigDao extends BaseMapper<SignInGlobalConfigEntity> {

	@Select("select * from sign_in_global_config where status = 1 order by id asc limit 1")
	SignInGlobalConfigEntity findEnabledOne();

	@Update("<script>update sign_in_global_config set status = 0, gmtModified = NOW() "
			+ "where status = 1"
			+ "<if test='excludeId != null'> and id &lt;&gt; #{excludeId}</if>"
			+ "</script>")
	int disableOthers(@Param("excludeId") Integer excludeId);
}
