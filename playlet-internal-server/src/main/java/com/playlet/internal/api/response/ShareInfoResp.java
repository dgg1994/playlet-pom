package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 播放页内容分享素材（不含视频播放地址）。
 */
@Data
@ApiModel(value = "分享素材", description = "分享面板 + H5 落地展示；不含 videoUrl")
public class ShareInfoResp {

	@ApiModelProperty("短剧ID")
	private Integer dramaId;

	@ApiModelProperty("集ID；非法或不传时可为 null")
	private Integer videoId;

	@ApiModelProperty("分享标题")
	private String title;

	@ApiModelProperty("分享描述（截断，给微信/系统分享）")
	private String desc;

	@ApiModelProperty("落地页正文（完整简介）")
	private String detail;

	@ApiModelProperty("封面宣传语")
	private String slogan;

	@ApiModelProperty("封面（已签名）")
	private String coverUrl;

	@ApiModelProperty("可传播落地链接")
	private String shareUrl;

	@ApiModelProperty("标签名称")
	private List<String> tags;

	@ApiModelProperty("总集数")
	private Integer totalEpisodes;

	@ApiModelProperty("年份（由创建/上架时间推导）")
	private Integer year;

	@ApiModelProperty("地区文案")
	private String region;

	@ApiModelProperty("当前剧分享量")
	private Long shareScore;
}
