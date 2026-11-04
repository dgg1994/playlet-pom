package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Banner 轮播项（用于 C 端首页热榜替代短剧轮播）
 */
@Data
@ApiModel("Banner 轮播项")
public class BannerCarouselItemResp {

    @ApiModelProperty("标题")
    private String title;

    @ApiModelProperty("列表缩略短文案")
    private String previewText;

    @ApiModelProperty("图片 URL（已签名）")
    private String imageUrl;

    @ApiModelProperty("跳转类型：1=活动H5 2=短剧详情 3=会员开通页")
    private Integer jumpType;

    @ApiModelProperty("jumpType=1 完整URL；=2 短剧ID字符串；=3 空字符串")
    private String jumpValue;
}
