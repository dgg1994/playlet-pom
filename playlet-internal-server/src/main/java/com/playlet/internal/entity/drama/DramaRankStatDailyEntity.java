package com.playlet.internal.entity.drama;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("drama_rank_stat_daily")
@ApiModel(value = "短剧榜单日聚合", description = "按自然日汇总观看/收藏/点赞等，供算法榜使用")
public class DramaRankStatDailyEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键ID", dataType = "Long")
	private Long id;

	@TableField("biz_date")
	@ApiModelProperty(name = "bizDate", value = "业务日 yyyy-MM-dd（Asia/Shanghai）", dataType = "String")
	private String bizDate;

	@TableField("drama_id")
	@ApiModelProperty(name = "dramaId", value = "短剧主键 drama.id", dataType = "Integer")
	private Integer dramaId;

	@TableField("play_pv")
	@ApiModelProperty(name = "playPv", value = "当日观看上报次数", dataType = "Integer")
	private Integer playPv;

	@TableField("valid_seconds")
	@ApiModelProperty(name = "validSeconds", value = "当日有效观看秒数累计", dataType = "Integer")
	private Integer validSeconds;

	@TableField("collect_cnt")
	@ApiModelProperty(name = "collectCnt", value = "当日净收藏次数（收藏+1/取消-1）", dataType = "Integer")
	private Integer collectCnt;

	@TableField("like_cnt")
	@ApiModelProperty(name = "likeCnt", value = "当日净点赞次数（点赞+1/取消-1）", dataType = "Integer")
	private Integer likeCnt;

	@TableField("comment_cnt")
	@ApiModelProperty(name = "commentCnt", value = "当日评论次数（预留）", dataType = "Integer")
	private Integer commentCnt;

	@TableField("search_cnt")
	@ApiModelProperty(name = "searchCnt", value = "当日搜索命中次数（预留）", dataType = "Integer")
	private Integer searchCnt;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "该日聚合行首次创建时间", dataType = "Date")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "最近一次累加/更新时间", dataType = "Date")
	private Date gmtModified;
}
