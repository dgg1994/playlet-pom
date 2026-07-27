package com.playlet.internal.dao.medal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.api.response.MedalApiResponse;
import com.playlet.internal.entity.medal.MedalConfigEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedalConfigDao extends BaseMapper<MedalConfigEntity> {

	@Select("select * from medal_config where medal_code = #{medalCode} and is_deleted = 0 limit 1")
	MedalConfigEntity findByMedalCode(@Param("medalCode") String medalCode);

	@Select("select * from medal_config where action_type = #{actionType} and status = 1 and is_deleted = 0 "
			+ "order by sort_weight desc, id asc")
	List<MedalConfigEntity> findEnabledByActionType(@Param("actionType") String actionType);

	@Select("<script>"
			+ "select c.*, i.medal_name as medalName from medal_config c "
			+ "left join medal_config_i18n i on i.medal_id = c.id "
			+ "<if test='langue != null and langue != \"\"'> and i.langue = #{langue} </if>"
			+ "where c.is_deleted = 0 "
			+ "<if test='medalCode != null and medalCode != \"\"'> and c.medal_code like concat('%',#{medalCode},'%') </if>"
			+ "<if test='actionType != null and actionType != \"\"'> and c.action_type = #{actionType} </if>"
			+ "<if test='status != null'> and c.status = #{status} </if>"
			+ "<if test='medalName != null and medalName != \"\"'> and i.medal_name like concat('%',#{medalName},'%') </if>"
			+ "order by c.sort_weight desc, c.id asc"
			+ "</script>")
	List<MedalConfigEntity> findAdminList(MedalConfigEntity entity);

	@Update("update medal_config set is_deleted = 1, gmtModified = NOW() where id = #{id}")
	int softDelete(@Param("id") Integer id);

	@Select("select id,icon_locked_key as logo from medal_config where is_deleted = 0 and status = 1 order by sort_weight desc")
	List<MedalApiResponse> selectLogoList();

	@Select("SELECT " +
			"  CASE " +
			"    WHEN um.unlocked = 1 THEN mc.icon_key " +
			"    ELSE mc.icon_locked_key " +
			"  END AS logo, " +
			"  mc.id AS id " +
			"FROM medal_config mc " +
			"LEFT JOIN user_medal um " +
			"  ON um.medal_id = mc.id " +
			"  AND um.uid = #{uid} " +
			"WHERE mc.status = 1 " +
			"  AND mc.is_deleted = 0")
	List<MedalApiResponse> selectLogoListByUid(@Param("uid") Integer uid);
}
