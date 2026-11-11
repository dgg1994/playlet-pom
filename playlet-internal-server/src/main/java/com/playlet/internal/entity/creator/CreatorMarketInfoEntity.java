package com.playlet.internal.entity.creator;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 人才市场展示信息。
 */
@Data
@TableName("creator_market_info")
@ApiModel(value = "人才市场展示信息", description = "人才市场（找编剧/找创作者）展示卡片与详情数据")
public class CreatorMarketInfoEntity {

	@TableId(type = IdType.AUTO)
	@ApiModelProperty(name = "id", value = "主键")
	private Integer id;

	@TableField("creator_id")
	@ApiModelProperty(name = "creatorId", value = "关联 creator_account.id（软引用，可空）")
	private Integer creatorId;

	@TableField("market_role")
	@ApiModelProperty(name = "marketRole", value = "市场角色 1编剧 2创作者(制作团队)")
	private Integer marketRole;

	@TableField("name")
	@ApiModelProperty(name = "name", value = "展示名称")
	private String name;

	@TableField("avatar")
	@ApiModelProperty(name = "avatar", value = "头像 key")
	private String avatar;

	@TableField("qual_type")
	@ApiModelProperty(name = "qualType", value = "资质类型 1个人 2公司 3团队")
	private Integer qualType;

	@TableField("badge")
	@ApiModelProperty(name = "badge", value = "徽章：认证编剧/明星编剧/全流程制作等")
	private String badge;

	@TableField("certified")
	@ApiModelProperty(name = "certified", value = "是否已认证 1是 0否")
	private Integer certified;

	@TableField("production")
	@ApiModelProperty(name = "production", value = "制作类型(创作者)")
	private String production;

	@TableField("skills")
	@ApiModelProperty(name = "skills", value = "技能 JSON 数组")
	private String skills;

	@TableField("tags")
	@ApiModelProperty(name = "tags", value = "擅长题材/标签 JSON 数组")
	private String tags;

	@TableField("style")
	@ApiModelProperty(name = "style", value = "技术风格")
	private String style;

	@TableField("services")
	@ApiModelProperty(name = "services", value = "承接服务 JSON 数组")
	private String services;

	@TableField("price")
	@ApiModelProperty(name = "price", value = "起价（元）")
	private BigDecimal price;

	@TableField("price_unit")
	@ApiModelProperty(name = "priceUnit", value = "计价单位 部/集")
	private String priceUnit;

	@TableField("score")
	@ApiModelProperty(name = "score", value = "综合评分 0-10")
	private BigDecimal score;

	@TableField("deals")
	@ApiModelProperty(name = "deals", value = "成交单数")
	private Integer deals;

	@TableField("works")
	@ApiModelProperty(name = "works", value = "作品数")
	private Integer works;

	@TableField("revenue")
	@ApiModelProperty(name = "revenue", value = "累计收益（元）")
	private Long revenue;

	@TableField("praise_rate")
	@ApiModelProperty(name = "praiseRate", value = "好评率（%）")
	private Integer praiseRate;

	@TableField("city")
	@ApiModelProperty(name = "city", value = "所在地")
	private String city;

	@TableField("accept_status")
	@ApiModelProperty(name = "acceptStatus", value = "接单状态 1可接单 2排期中")
	private Integer acceptStatus;

	@TableField("intro")
	@ApiModelProperty(name = "intro", value = "详情页简介")
	private String intro;

	@TableField("join_date")
	@ApiModelProperty(name = "joinDate", value = "入驻时间展示（yyyy-MM）")
	private String joinDate;

	@TableField("avg_delivery_days")
	@ApiModelProperty(name = "avgDeliveryDays", value = "平均交付周期（天）")
	private Integer avgDeliveryDays;

	@TableField("works_list")
	@ApiModelProperty(name = "worksList", value = "详情-作品列表 JSON")
	private String worksList;

	@TableField("scripts_list")
	@ApiModelProperty(name = "scriptsList", value = "详情-在售剧本 JSON（编剧）")
	private String scriptsList;

	@TableField("packages")
	@ApiModelProperty(name = "packages", value = "详情-服务套餐 JSON（创作者）")
	private String packages;

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
