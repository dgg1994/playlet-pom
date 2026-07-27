package com.playlet.internal.dao.medal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.medal.MedalConfigI18nEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedalConfigI18nDao extends BaseMapper<MedalConfigI18nEntity> {

	@Select("select * from medal_config_i18n where medal_id = #{medalId}")
	List<MedalConfigI18nEntity> findByMedalId(@Param("medalId") Integer medalId);

	@Delete("delete from medal_config_i18n where medal_id = #{medalId}")
	int deleteByMedalId(@Param("medalId") Integer medalId);

	@Select("select medal_name from medal_config_i18n where medal_id = #{medalId} and langue = #{language} limit 1")
	String selectNameByMedalId(@Param("medalId") Integer medalId, @Param("language") String language);

	@Select("select * from medal_config_i18n where medal_id = #{medalId} and langue = #{language} limit 1")
	MedalConfigI18nEntity findByMedalIdAndLangue(@Param("medalId") Integer medalId,
			@Param("language") String language);
}
