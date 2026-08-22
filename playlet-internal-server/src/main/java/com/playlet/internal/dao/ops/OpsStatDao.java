package com.playlet.internal.dao.ops;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 运营看板聚合查询（跨表）。
 */
@Repository
public interface OpsStatDao {

	/** 区间新增用户（按注册 setTime，上海自然日边界由调用方传入 datetime 字符串） */
	@Select("select ifnull(count(1), 0) from app_account "
			+ "where setTime >= #{startTime} and setTime < #{endTimeExclusive}")
	long countNewUsers(@Param("startTime") String startTime, @Param("endTimeExclusive") String endTimeExclusive);

	/** 区间总播放秒：剧维度日表（与榜单同源，可作兜底） */
	@Select("select ifnull(sum(valid_seconds), 0) from drama_rank_stat_daily "
			+ "where biz_date >= #{startDate} and biz_date <= #{endDate}")
	long sumDramaPlaySeconds(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
