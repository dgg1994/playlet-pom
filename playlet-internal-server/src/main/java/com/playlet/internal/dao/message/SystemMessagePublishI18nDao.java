package com.playlet.internal.dao.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.message.SystemMessagePublishI18nEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMessagePublishI18nDao extends BaseMapper<SystemMessagePublishI18nEntity> {

	@Select("select * from system_message_publish_i18n where publish_id = #{publishId}")
	List<SystemMessagePublishI18nEntity> findByPublishId(@Param("publishId") Long publishId);

	@Select("select * from system_message_publish_i18n where publish_id = #{publishId} "
			+ "and langue = #{langue} limit 1")
	SystemMessagePublishI18nEntity findByPublishIdAndLangue(@Param("publishId") Long publishId,
			@Param("langue") String langue);

	@Delete("delete from system_message_publish_i18n where publish_id = #{publishId}")
	int deleteByPublishId(@Param("publishId") Long publishId);

	@Select("<script>"
			+ "select * from system_message_publish_i18n where publish_id in "
			+ "<foreach collection='publishIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
			+ "</script>")
	List<SystemMessagePublishI18nEntity> findByPublishIds(@Param("publishIds") List<Long> publishIds);
}
