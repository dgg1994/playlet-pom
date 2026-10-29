package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 会员状态出参。
 */
@Data
@ApiModel("会员状态")
public class MemberStatusRespEntity {

	@ApiModelProperty("是否有效会员：已开通且未到期")
	private Boolean isMember;

	@ApiModelProperty("会员到期时间；非会员为 null")
	private Date expireTime;

	@ApiModelProperty("是否免广告")
	private Boolean adFree;

	@ApiModelProperty("是否可免费看会员短剧")
	private Boolean vipDramaFree;

	@ApiModelProperty("是否开启连续包月自动续费")
	private Boolean autoRenew;

	@ApiModelProperty("下次自动扣款时间")
	private Date nextDeductTime;
}
