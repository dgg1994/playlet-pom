package com.playlet.oversea.dao.medal;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.api.response.MedalApiResponse;
import com.playlet.oversea.entity.medal.MedalConfigEntity;
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

	@Select("select mc.*,mci.medal_name as medalName,mci.slogan as slogan,mci.condition_text as  conditionText " +
			"from medal_config mc left join medal_config_i18n mci on mc.id = mci.medal_id" +
			" where is_deleted = 0 and status = 1 and mci.langue = #{langue} order by sort_weight desc")
	List<MedalConfigEntity> selectLogoList(@Param("langue") String langue);

	@Select("SELECT " +
			"  mc.*, " +
			"  mci.medal_name AS medalName, " +
			"  mci.slogan AS slogan, " +
			"  mci.condition_text AS conditionText, " +
			"  um.unlock_time AS unlockTime, " +
			"  CASE " +
			"    WHEN um.unlocked = 1 THEN mc.icon_key " +
			"    ELSE mc.icon_locked_key " +
			"  END AS logo " +
			"FROM medal_config mc " +
			"LEFT JOIN medal_config_i18n mci " +
			"  ON mc.id = mci.medal_id AND mci.langue = #{langue} " +
			"LEFT JOIN user_medal um " +
			"  ON um.medal_id = mc.id AND um.uid = #{uid} " +
			"WHERE mc.status = 1 " +
			"  AND mc.is_deleted = 0 " +
			"ORDER BY mc.sort_weight DESC, mc.id ASC")
	List<MedalConfigEntity> selectLogoListByUid(@Param("uid") Integer uid, @Param("langue") String langue);
}
