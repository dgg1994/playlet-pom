package com.playlet.oversea.query.drama;

import com.baomidou.mybatisplus.annotation.TableField;
import com.playlet.oversea.query.pub.PageQueryHelperEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InteractMessageQuery extends PageQueryHelperEntity {

	@TableField(exist = false)
	@ApiModelProperty(name = "messageType", value = "消息类型 LIKE_DRAMA/LIKE_COMMENT/COMMENT_DRAMA/COMMENT_VIDEO/REPLY_DRAMA/REPLY_VIDEO", required = false, dataType = "String")
	private String messageType;

	@TableField(exist = false)
	@ApiModelProperty(name = "isRead", value = "是否已读 0未读1已读", required = false, dataType = "Integer")
	private Integer isRead;

	@TableField(exist = false)
	private Integer toUid;
}
