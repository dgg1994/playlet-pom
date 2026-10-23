package com.playlet.internal.api.request;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端短剧付费列表入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "短剧付费查询", description = "按短剧维度统计付费收入")
public class DramaPaymentQuery extends PageQueryHelperEntity {

	@ApiModelProperty("短剧名称或短剧ID")
	private String dramaName;

	@ApiModelProperty("分类：标签 groupId 或标签名称关键词")
	private String dramaTag;

	@ApiModelProperty("付费模式：MEMBER 会员剧 / PAY 单集付费 / AD 广告解锁；空=全部")
	private String payType;
}
