package com.playlet.internal.dao.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.system.SyssCountryCodeEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SyssCountryCodeDao extends BaseMapper<SyssCountryCodeEntity>{

	@Select("select * from sys_country_code where language = #{language}")
	List<SyssCountryCodeEntity> findLanguage(@Param("language") String language);


}
