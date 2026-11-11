package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.playlet.internal.config.jackson.JsonFlexibleStringDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 征稿广场征稿信息。
 */
@Data
@TableName("creator_call")
@ApiModel(value = "征稿信息", description = "征稿广场列表卡片与详情数据")
public class CreatorCallEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("publisher_id")
	@ApiModelProperty(name = "publisherId", value = "征稿方 creator_account.id")
	private Integer publisherId;

	@TableField("publisher")
	@ApiModelProperty(name = "publisher", value = "征稿方名称")
	private String publisher;

	@TableField("title")
	@ApiModelProperty(name = "title", value = "征稿标题")
	private String title;

	@TableField("topic")
	@ApiModelProperty(name = "topic", value = "题材标签")
	private String topic;

	@TableField("exclusive")
	@ApiModelProperty(name = "exclusive", value = "是否独家 1独家 0非独家")
	private Integer exclusive;

	@TableField("amount")
	@ApiModelProperty(name = "amount", value = "总奖金（元）")
	private Long amount;

	@TableField("need_count")
	@ApiModelProperty(name = "needCount", value = "需求稿件数")
	private Integer needCount;

	@TableField("participants")
	@ApiModelProperty(name = "participants", value = "已参与人数")
	private Integer participants;

	@TableField("deadline")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@ApiModelProperty(name = "deadline", value = "征稿截止日期")
	private Date deadline;

	@TableField("publish_at")
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@ApiModelProperty(name = "publishAt", value = "发布时间")
	private Date publishAt;

	@TableField("tags")
	@JsonDeserialize(using = JsonFlexibleStringDeserializer.class)
	@ApiModelProperty(name = "tags", value = "卡片标签 JSON 数组")
	private String tags;

	@TableField("intro")
	@ApiModelProperty(name = "intro", value = "卡片摘要")
	private String intro;

	@TableField("benchmark")
	@ApiModelProperty(name = "benchmark", value = "对标作品")
	private String benchmark;

	@TableField("duration_per_episode")
	@ApiModelProperty(name = "durationPerEpisode", value = "单集时长（分钟）")
	private BigDecimal durationPerEpisode;

	@TableField("episodes")
	@ApiModelProperty(name = "episodes", value = "集数")
	private Integer episodes;

	@TableField("content_requirement")
	@ApiModelProperty(name = "contentRequirement", value = "详情-内容要求")
	private String contentRequirement;

	@TableField("lead_cast")
	@ApiModelProperty(name = "leadCast", value = "详情-主演资料")
	private String leadCast;

	@TableField("support_cast")
	@ApiModelProperty(name = "supportCast", value = "详情-助演资料")
	private String supportCast;

	@TableField("benchmark_note")
	@ApiModelProperty(name = "benchmarkNote", value = "详情-参考对标详情")
	private String benchmarkNote;

	@TableField("extra_note")
	@ApiModelProperty(name = "extraNote", value = "详情-其他补充")
	private String extraNote;

	@TableField("selection_days")
	@ApiModelProperty(name = "selectionDays", value = "选稿周期")
	private String selectionDays;

	@TableField("settle")
	@ApiModelProperty(name = "settle", value = "结算方式")
	private String settle;

	@TableField("attachments")
	@JsonDeserialize(using = JsonFlexibleStringDeserializer.class)
	@ApiModelProperty(name = "attachments", value = "详情-附件 JSON")
	private String attachments;

	@TableField("submissions")
	@ApiModelProperty(name = "submissions", value = "详情-已投稿剧本 JSON")
	private String submissions;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "综合排序权重")
	private Integer sortWeight;

	@TableField("shelf_status")
	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0下架 1上架")
	private Integer shelfStatus;

	@TableField("delete_state")
	@ApiModelProperty(name = "deleteState", value = "删除状态 0正常 1删除")
	private Integer deleteState;

	@TableField("setTime")
	@ApiModelProperty(name = "setTime", value = "创建时间")
	private Date setTime;

	@TableField("gmtModified")
	@ApiModelProperty(name = "gmtModified", value = "更新时间")
	private Date gmtModified;
}
