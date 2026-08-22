package com.playlet.internal.dao.ops;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.ops.UserActiveDailyEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface UserActiveDailyDao extends BaseMapper<UserActiveDailyEntity> {

	/** 日活打点：已存在则只更新 gmtModified */
	@Insert("insert into user_active_daily (biz_date, uid, setTime, gmtModified) "
			+ "values (#{bizDate}, #{uid}, now(), now()) "
			+ "on duplicate key update gmtModified = now()")
	int upsertActive(@Param("bizDate") String bizDate, @Param("uid") Integer uid);

	/** 区间内去重活跃用户数（多日合并 DAU 口径用 person-day 请用 sumDailyDau） */
	@Select("select ifnull(count(distinct uid), 0) from user_active_daily "
			+ "where biz_date >= #{startDate} and biz_date <= #{endDate}")
	long countDistinctUid(@Param("startDate") String startDate, @Param("endDate") String endDate);

	/** 单日 DAU */
	@Select("select ifnull(count(1), 0) from user_active_daily where biz_date = #{bizDate}")
	long countByDate(@Param("bizDate") String bizDate);

	/** 区间 person-day（每日 DAU 之和，用于人均分母） */
	@Select("select ifnull(count(1), 0) from user_active_daily "
			+ "where biz_date >= #{startDate} and biz_date <= #{endDate}")
	long sumPersonDays(@Param("startDate") String startDate, @Param("endDate") String endDate);

	/**
	 * 区间内各日次日留存率均值（百分比）。
	 * 仅统计「注册日 D 且 D+1 &lt;= endDate」的 cohort；无新增的日子不计。
	 */
	@Select("select ifnull(avg(t.rate), 0) from ("
			+ "  select "
			+ "    date_format(a.setTime, '%Y-%m-%d') as cohort_date, "
			+ "    count(distinct a.id) as new_cnt, "
			+ "    count(distinct case when u.uid is not null then a.id end) as retained_cnt, "
			+ "    (count(distinct case when u.uid is not null then a.id end) * 100.0 "
			+ "      / nullif(count(distinct a.id), 0)) as rate "
			+ "  from app_account a "
			+ "  left join user_active_daily u on u.uid = a.id "
			+ "    and u.biz_date = date_format(date_add(a.setTime, interval 1 day), '%Y-%m-%d') "
			+ "  where a.setTime >= #{cohortStart} and a.setTime < #{cohortEndExclusive} "
			+ "  group by date_format(a.setTime, '%Y-%m-%d') "
			+ "  having new_cnt > 0 "
			+ ") t")
	BigDecimal avgRetentionD1Rate(@Param("cohortStart") String cohortStart,
			@Param("cohortEndExclusive") String cohortEndExclusive);
}
