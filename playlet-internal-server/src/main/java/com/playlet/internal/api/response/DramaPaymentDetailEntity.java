package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 短剧付费详情汇总（KPI + 剧信息 + 剧集付费摘要）。
 */
@Data
@ApiModel(value = "短剧付费详情", description = "短剧维度付费详情页数据")
public class DramaPaymentDetailEntity {

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("短剧名称")
	private String dramaName;

	@ApiModelProperty("封面URL")
	private String coverUrl;

	@ApiModelProperty("分类标签（顿号拼接）")
	private String dramaTag;

	@ApiModelProperty("上架状态 0未上架 1已上架")
	private Integer shelfStatus;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("付费集数")
	private Integer paidEpisodes;

	@ApiModelProperty("免费集数")
	private Integer freeEpisodes;

	@ApiModelProperty("上架/首发时间")
	private Date releaseTime;

	@ApiModelProperty("出品方")
	private String producerFirm;

	@ApiModelProperty("是否会员剧 0否 1是")
	private Integer isMemberDrama;

	@ApiModelProperty("是否支持广告解锁")
	private Boolean adUnlockSupported;

	@ApiModelProperty("单集默认价(元，付费集均价)")
	private BigDecimal episodePriceAvg;

	@ApiModelProperty("单集原价参考(元，付费集最高价)")
	private BigDecimal episodePriceOrigin;

	@ApiModelProperty("累计收入(元)")
	private BigDecimal totalRevenue;

	@ApiModelProperty("较上月收入涨跌幅(%)，无上月数据为 null")
	private BigDecimal revenueMomRate;

	@ApiModelProperty("购买人次")
	private Integer purchaseCount;

	@ApiModelProperty("退款率(%)")
	private BigDecimal refundRate;

	@ApiModelProperty("付费集总数（剧集明细合计）")
	private Integer episodePayTotal;

	@ApiModelProperty("剧集付费摘要列表（按收入倒序截断）")
	private List<DramaEpisodePayItemEntity> episodePayList;
}
