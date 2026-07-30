package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.UserInteractMessageEntity;
import com.playlet.internal.query.drama.InteractMessageQuery;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractMessageDao extends BaseMapper<UserInteractMessageEntity> {

	@Select("<script>"
			+ "select * from user_interact_message "
			+ "where to_uid = #{toUid} and status = 1 "
			+ "<if test='messageType != null and messageType != \"\"'> and message_type = #{messageType} </if>"
			+ "<if test='isRead != null'> and is_read = #{isRead} </if>"
			+ "order by setTime desc, id desc"
			+ "</script>")
	List<UserInteractMessageEntity> findByToUid(InteractMessageQuery entity);

	@Update("update user_interact_message set is_read = 1, read_time = now(), gmtModified = now() "
			+ "where id = #{id} and to_uid = #{toUid} and status = 1 and ifnull(is_read,0) = 0")
	int readOne(@Param("id") Long id, @Param("toUid") Integer toUid);

	@Update("update user_interact_message set is_read = 1, read_time = now(), gmtModified = now() "
			+ "where to_uid = #{toUid} and status = 1 and ifnull(is_read,0) = 0")
	int readAll(@Param("toUid") Integer toUid);

	@Select("select count(1) from user_interact_message where to_uid = #{toUid} and status = 1 and ifnull(is_read,0)=0")
	Integer countUnread(@Param("toUid") Integer toUid);
}
