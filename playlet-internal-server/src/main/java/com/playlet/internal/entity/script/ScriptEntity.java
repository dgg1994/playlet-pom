package com.playlet.internal.entity.script;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 剧本市场-剧本主表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("script")
@ApiModel(value = "剧本", description = "剧本市场主表")
public class ScriptEntity extends PageQueryHelperEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("script_name")
	@ApiModelProperty(name = "scriptName", value = "剧本名称")
	private String scriptName;

	@TableField("cover_url")
	@ApiModelProperty(name = "coverUrl", value = "封面七牛 key")
	private String coverUrl;

	@TableField("theme_partition")
	@ApiModelProperty(name = "themePartition", value = "内容分区 1女频 2男频 3合家欢")
	private Integer themePartition;

	@TableField("topic")
	@ApiModelProperty(name = "topic", value = "主题材")
	private String topic;

	@TableField("license_type")
	@ApiModelProperty(name = "licenseType", value = "授权类型 1独家 2普通 3可改编")
	private Integer licenseType;

	@TableField("license_scope")
	@ApiModelProperty(name = "licenseScope", value = "授权范围描述")
	private String licenseScope;

	@TableField("episodes")
	@ApiModelProperty(name = "episodes", value = "集数")
	private Integer episodes;

	@TableField("avg_minutes")
	@ApiModelProperty(name = "avgMinutes", value = "单集平均时长（分钟）")
	private BigDecimal avgMinutes;

	@TableField("price_yuan")
	@ApiModelProperty(name = "priceYuan", value = "售价（元）")
	private BigDecimal priceYuan;

	@TableField("sale_status")
	@ApiModelProperty(name = "saleStatus", value = "售卖状态 0待售 1议价中 2已售 3签约中")
	private Integer saleStatus;

	@TableField("score")
	@ApiModelProperty(name = "score", value = "评分 0-10")
	private BigDecimal score;

	@TableField("heat_score")
	@ApiModelProperty(name = "heatScore", value = "热度值")
	private Long heatScore;

	@TableField("script_type")
	@ApiModelProperty(name = "scriptType", value = "剧本类型 1真人短剧 2AI漫剧")
	private Integer scriptType;

	@TableField("finish_state")
	@ApiModelProperty(name = "finishState", value = "完成状态 0连载 1完结")
	private Integer finishState;

	@TableField("description")
	@ApiModelProperty(name = "description", value = "简介")
	private String description;

	@TableField("core_point")
	@ApiModelProperty(name = "corePoint", value = "核心看点")
	private String corePoint;

	@TableField("one_line")
	@ApiModelProperty(name = "oneLine", value = "一句话故事")
	private String oneLine;

	@TableField("benchmark_works")
	@ApiModelProperty(name = "benchmarkWorks", value = "对标作品")
	private String benchmarkWorks;

	@TableField("attachment_key")
	@ApiModelProperty(name = "attachmentKey", value = "剧本全文附件 key")
	private String attachmentKey;

	@TableField("author_id")
	@ApiModelProperty(name = "authorId", value = "编剧 creator_account.id")
	private Integer authorId;

	@TableField("audit_status")
	@ApiModelProperty(name = "auditStatus", value = "审核状态 -1草稿 0待审 1审核中 2通过 3驳回")
	private Integer auditStatus;

	@TableField("audit_reject_reason")
	@ApiModelProperty(name = "auditRejectReason", value = "审核驳回原因")
	private String auditRejectReason;

	@TableField("audit_time")
	@ApiModelProperty(name = "auditTime", value = "审核时间")
	private Date auditTime;

	@TableField("audit_pass_time")
	@ApiModelProperty(name = "auditPassTime", value = "审核通过时间")
	private Date auditPassTime;

	@TableField("auditor_id")
	@ApiModelProperty(name = "auditorId", value = "审核员 sys_user.id")
	private Integer auditorId;

	@TableField("shelf_status")
	@ApiModelProperty(name = "shelfStatus", value = "上架状态 0下架 1上架")
	private Integer shelfStatus;

	@TableField("shelf_time")
	@ApiModelProperty(name = "shelfTime", value = "上架时间")
	private Date shelfTime;

	@TableField("offline_reason")
	@ApiModelProperty(name = "offlineReason", value = "下架原因")
	private String offlineReason;

	@TableField("sort_weight")
	@ApiModelProperty(name = "sortWeight", value = "综合排序权重")
	private Integer sortWeight;

	@TableField("version")
	@ApiModelProperty(name = "version", value = "乐观锁版本号")
	private Integer version;

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
