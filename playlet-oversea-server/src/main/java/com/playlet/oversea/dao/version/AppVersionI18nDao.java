package com.playlet.oversea.dao.version;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.version.AppVersionI18nEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppVersionI18nDao extends BaseMapper<AppVersionI18nEntity> {

	@Select("select * from app_version_i18n where version_id = #{versionId}")
	List<AppVersionI18nEntity> findByVersionId(@Param("versionId") Integer versionId);

	@Delete("delete from app_version_i18n where version_id = #{versionId}")
	int deleteByVersionId(@Param("versionId") Integer versionId);

	@Select("select title from app_version_i18n where version_id = #{versionId} and langue = #{language} limit 1")
	String selectTitleByVersionId(@Param("versionId") Integer versionId, @Param("language") String language);

	@Select("select * from app_version_i18n where version_id = #{versionId} and langue = #{language} limit 1")
	AppVersionI18nEntity findByVersionIdAndLangue(@Param("versionId") Integer versionId,
			@Param("language") String language);
}
