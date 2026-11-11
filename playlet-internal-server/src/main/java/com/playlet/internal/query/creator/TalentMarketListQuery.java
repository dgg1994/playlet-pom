package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 人才市场列表查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "人才市场列表查询", description = "分类 + 筛选 + 排序 + 分页")
public class TalentMarketListQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "role", value = "市场角色 1编剧 2创作者，默认 1")
	private Integer role;

	@ApiModelProperty(name = "keyword", value = "关键词：名称/徽章/标签/服务/技能/城市")
	private String keyword;

	@ApiModelProperty(name = "qualification", value = "资质类型：个人/公司/团队，全部表示不筛")
	private String qualification;

	@ApiModelProperty(name = "service", value = "服务类型（编剧）：原创剧本/剪辑制作/拍摄服务/AIGC制作")
	private String service;

	@ApiModelProperty(name = "production", value = "制作类型（创作者）：拍摄/剪辑/全流程/AIGC制作")
	private String production;

	@ApiModelProperty(name = "skill", value = "技术类型/技术详情：剪辑/调色/特效包装/配音/字幕等")
	private String skill;

	@ApiModelProperty(name = "style", value = "技术风格：电影感/原创剧情/快节奏爽感/古风唯美/现代写实")
	private String style;

	@ApiModelProperty(name = "contract", value = "承接服务：原创定制/IP改编/单集定制/全流程制作/后期剪辑")
	private String contract;

	@ApiModelProperty(name = "priceRange", value = "价格 0全部 1<1万 2 1-5万 3 5-10万 4>10万")
	private Integer priceRange;

	@ApiModelProperty(name = "sortType", value = "1综合 2成交最多 3好评最高 4价格最低 5作品最多 6累计收益最高")
	private Integer sortType;
}
