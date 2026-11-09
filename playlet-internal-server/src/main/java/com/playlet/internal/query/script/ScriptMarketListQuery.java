package com.playlet.internal.query.script;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端剧本市场列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "剧本市场列表查询", description = "筛选 + 排序 + 分页")
public class ScriptMarketListQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "keyword", value = "关键词：标题/编剧/标签/简介")
	private String keyword;

	@ApiModelProperty(name = "topic", value = "题材，all 表示全部")
	private String topic;

	@ApiModelProperty(name = "channel", value = "内容分区 0全部 1女频 2男频 3合家欢")
	private Integer channel;

	@ApiModelProperty(name = "licenseType", value = "授权类型 0全部 1独家 2普通 3可改编")
	private Integer licenseType;

	@ApiModelProperty(name = "priceRange", value = "价格 0全部 1<1万 2 1-5万 3 5-10万 4>10万")
	private Integer priceRange;

	@ApiModelProperty(name = "scaleRange", value = "规模 0全部 1<10集 2 10-30集 3 30-60集 4>60集")
	private Integer scaleRange;

	@ApiModelProperty(name = "saleStatus", value = "售卖状态 -1全部 0待售 1议价中 2已售 3签约中")
	private Integer saleStatus;

	@ApiModelProperty(name = "sortType", value = "1综合 2最新 3评分 4价格 5热度")
	private Integer sortType;
}
