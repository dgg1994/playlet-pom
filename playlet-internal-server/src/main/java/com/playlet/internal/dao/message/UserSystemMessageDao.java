package com.playlet.internal.dao.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.message.UserSystemMessageEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSystemMessageDao extends BaseMapper<UserSystemMessageEntity> {

	@Select("select * from user_system_message where to_uid = #{toUid} and status = 1 "
			+ "order by setTime desc, id desc")
	List<UserSystemMessageEntity> findByToUid(@Param("toUid") Integer toUid);

	@Select("select count(1) from user_system_message where to_uid = #{toUid} and status = 1 "
			+ "and ifnull(is_read,0)=0")
	Integer countUnread(@Param("toUid") Integer toUid);

	@Update("update user_system_message set is_read = 1, gmtModified = now() "
			+ "where id = #{id} and to_uid = #{toUid} and status = 1")
	int readOne(@Param("id") Long id, @Param("toUid") Integer toUid);

	@Update("update user_system_message set is_read = 1, gmtModified = now() "
			+ "where to_uid = #{toUid} and status = 1 and ifnull(is_read,0)=0")
	int readAll(@Param("toUid") Integer toUid);

	@Select("select * from user_system_message where to_uid = #{toUid} and biz_id = #{bizId} limit 1")
	UserSystemMessageEntity findByBiz(@Param("toUid") Integer toUid, @Param("bizId") String bizId);
}
