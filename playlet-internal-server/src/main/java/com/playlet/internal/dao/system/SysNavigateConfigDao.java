package com.playlet.internal.dao.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.system.SysNavigateConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysNavigateConfigDao extends BaseMapper<SysNavigateConfigEntity>{

	@Select("select * from sys_navigate_config where app_version = #{appVersion} and device_type = #{deviceType}")
	SysNavigateConfigEntity findOne(@Param("appVersion") String appVersion,@Param("deviceType") String deviceType);

	@Select("<script>"
			+ "select * from sys_navigate_config where 1=1"
			+ "<if test = 'appVersion != null'> and app_version = #{appVersion}</if>"
			+ "<if test = 'deviceType != null'> and device_type = #{deviceType}</if>"
			+ "order by device_type,app_version desc"
			+ "</script>")
	List<SysNavigateConfigEntity> findList(SysNavigateConfigEntity configEntity);

}
