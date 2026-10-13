package com.playlet.internal.dao.drama;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.drama.DramaDanmuEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 短剧弹幕 Dao。
 */
@Repository
public interface DramaDanmuDao extends BaseMapper<DramaDanmuEntity> {

	/**
	 * 按集 + 播放时间窗拉取可展示弹幕（播放器用）。
	 */
	@Select("select d.id, d.drama_id, d.video_id, d.uid, d.content, d.play_time, "
			+ "d.color, d.mode, d.font_size, d.status, d.delete_state, d.setTime, d.gmtModified, "
			+ "a.nickname as nickname, a.avatar as avatar "
			+ "from drama_danmu d "
			+ "left join app_account a on a.id = d.uid "
			+ "where d.video_id = #{videoId} "
			+ "and d.delete_state = #{deleteState} "
			+ "and d.status = #{status} "
			+ "and d.play_time >= #{fromTime} "
			+ "and d.play_time <= #{toTime} "
			+ "order by d.play_time asc, d.id asc "
			+ "limit #{limit}")
	List<DramaDanmuEntity> findByVideoAndPlayTimeRange(@Param("videoId") Integer videoId,
			@Param("fromTime") Integer fromTime,
			@Param("toTime") Integer toTime,
			@Param("status") Integer status,
			@Param("deleteState") Integer deleteState,
			@Param("limit") int limit);

	/**
	 * 按主键查未软删记录。
	 */
	@Select("select * from drama_danmu where id = #{id} and delete_state = #{deleteState} limit 1")
	DramaDanmuEntity findByIdAndDeleteState(@Param("id") Long id, @Param("deleteState") Integer deleteState);

	/**
	 * 用户自删：标记 status=用户自删，并软删。
	 */
	@Update("update drama_danmu set status = #{status}, delete_state = #{deleteState}, "
			+ "gmtModified = now() "
			+ "where id = #{id} and uid = #{uid} and delete_state = 0")
	int softDeleteByIdAndUid(@Param("id") Long id,
			@Param("uid") Integer uid,
			@Param("status") Integer status,
			@Param("deleteState") Integer deleteState);

	/**
	 * 运营隐藏（审核不通过等）。
	 */
	@Update("update drama_danmu set status = #{status}, gmtModified = now() "
			+ "where id = #{id} and delete_state = 0")
	int updateStatusById(@Param("id") Long id, @Param("status") Integer status);

	/**
	 * 统计用户对某集的发送条数（频控用，按时间范围可选）。
	 */
	@Select("<script>"
			+ "select count(1) from drama_danmu "
			+ "where uid = #{uid} and video_id = #{videoId} and delete_state = 0 "
			+ "<if test='afterTime != null'> and setTime >= #{afterTime} </if>"
			+ "</script>")
	int countByUidAndVideo(@Param("uid") Integer uid,
			@Param("videoId") Integer videoId,
			@Param("afterTime") java.util.Date afterTime);
}
