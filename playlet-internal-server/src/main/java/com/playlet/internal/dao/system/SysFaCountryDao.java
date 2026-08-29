package com.playlet.internal.dao.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.system.SysFaCountryEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysFaCountryDao extends BaseMapper<SysFaCountryEntity>{

	@Select("select * from sys_fa_country where parentId = #{parentId}")
	List<SysFaCountryEntity> findParent(@Param("parentId") Integer parentId);

}
