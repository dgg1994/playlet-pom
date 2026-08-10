package com.playlet.oversea.dao.version;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.version.AppVersionConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppVersionConfigDao extends BaseMapper<AppVersionConfigEntity> {

	@Select("select * from app_version_config "
			+ "where platform = #{platform} and channel = #{channel} "
			+ "and version_code = #{versionCode} limit 1")
	AppVersionConfigEntity findByUnique(@Param("platform") String platform,
			@Param("channel") String channel,
			@Param("versionCode") Integer versionCode);

	@Select("<script>"
			+ "select c.*, i.title as title from app_version_config c "
			+ "left join app_version_i18n i on i.version_id = c.id "
			+ "<if test='langue != null and langue != \"\"'> and i.langue = #{langue} </if>"
			+ "where 1=1 "
			+ "<if test='platform != null and platform != \"\"'> and c.platform = #{platform} </if>"
			+ "<if test='channel != null and channel != \"\"'> and c.channel = #{channel} </if>"
			+ "<if test='versionName != null and versionName != \"\"'> and c.version_name like concat('%',#{versionName},'%') </if>"
			+ "<if test='status != null'> and c.status = #{status} </if>"
			+ "<if test='isForce != null'> and c.is_force = #{isForce} </if>"
			+ "order by c.version_code desc, c.id desc"
			+ "</script>")
	List<AppVersionConfigEntity> findAdminList(AppVersionConfigEntity entity);

	@Select("select * from app_version_config "
			+ "where platform = #{platform} "
			+ "and channel = #{channel} "
			+ "and status = 1 "
			+ "order by version_code desc, id desc "
			+ "limit 1")
	AppVersionConfigEntity findLatestForCheck(@Param("platform") String platform,
			@Param("channel") String channel);

	@Update("update app_version_config set status = #{statusDisable} where platform = #{platform} and version_name != #{versionName}")
    void updateStatusByPlatformAndChannel(@Param("statusDisable") Integer statusDisable,@Param("platform") String platform, @Param("versionName") String versionName);
}
