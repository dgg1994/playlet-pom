package com.playlet.internal.query.creator;

import com.playlet.internal.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作家端消息中心查询：分页 + 分类 + 时间区间。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "作家端消息查询")
public class CreatorMessageQuery extends PageQueryHelperEntity {

	@ApiModelProperty(name = "category", value = "分类 ALL全部 SYSTEM系统公告 AUDIT审核 COOP合作 SUBMIT征稿投稿 CONTRACT合同 INCOME收益结算")
	private String category;
}
