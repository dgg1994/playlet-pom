package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 17track 注册物流单号响应摘要。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmsTrackingRegisterResp {

	private DataInner data;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataInner {
		private List<RejectedItem> rejected;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class RejectedItem {
		private ErrorInfo error;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorInfo {
		private String message;
	}
}
