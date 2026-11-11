package com.playlet.internal.api.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * 创作者隐私 + 推送设置返回。
 */
@Data
@ApiModel(value = "创作者设置")
public class CreatorSettingResp {

	private Privacy privacy;
	private Push push;

	@Data
	@ApiModel(value = "隐私设置")
	public static class Privacy {
		private Integer profileVisible;
		private Integer contactVisible;
		private Integer workVisible;
	}

	@Data
	@ApiModel(value = "推送设置")
	public static class Push {
		private Integer systemNotice;
		private Integer coopMessage;
		private Integer activityPush;
		private Integer submitMessage;
		private Integer contractMessage;
		private Integer auditMessage;
		private Integer incomeNotice;
	}
}
