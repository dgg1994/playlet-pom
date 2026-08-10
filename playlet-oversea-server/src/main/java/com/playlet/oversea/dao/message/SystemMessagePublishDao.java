package com.playlet.oversea.dao.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.oversea.entity.message.SystemMessagePublishEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemMessagePublishDao extends BaseMapper<SystemMessagePublishEntity> {

	@Select("select * from system_message_publish where status = 1 and publish_status = 1 "
			+ "and audience_type = 1 "
			+ "and (valid_start is null or valid_start <= now()) "
			+ "and (valid_end is null or valid_end > now()) "
			+ "order by priority desc, id desc")
	List<SystemMessagePublishEntity> findActiveBroadcastList();

	@Select("select count(1) from system_message_publish where status = 1 and publish_status = 1 "
			+ "and audience_type = 1 "
			+ "and (valid_start is null or valid_start <= now()) "
			+ "and (valid_end is null or valid_end > now()) "
			+ "and id > #{afterId}")
	Integer countBroadcastUnread(@Param("afterId") Long afterId);

	@Select("select ifnull(max(id),0) from system_message_publish where status = 1 and publish_status = 1 "
			+ "and audience_type = 1")
	Long maxBroadcastId();

	@Select("<script>"
			+ "select * from system_message_publish where 1=1 "
			+ "<if test='messageType != null and messageType != \"\"'> and message_type = #{messageType} </if>"
			+ "<if test='publishStatus != null'> and publish_status = #{publishStatus} </if>"
			+ "<if test='status != null'> and status = #{status} </if>"
			+ " order by id desc"
			+ "</script>")
	List<SystemMessagePublishEntity> findAdminList(SystemMessagePublishEntity entity);

	@Update("update system_message_publish set publish_status = #{publishStatus}, gmtModified = now() "
			+ "where id = #{id}")
	int updatePublishStatus(@Param("id") Long id, @Param("publishStatus") Integer publishStatus);

	@Update("update system_message_publish set status = #{status}, gmtModified = now() where id = #{id}")
	int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
