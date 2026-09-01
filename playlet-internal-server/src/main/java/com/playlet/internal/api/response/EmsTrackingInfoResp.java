package com.playlet.internal.api.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 17track 物流查询响应（对齐 worldpay TrackingInfoEntity）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmsTrackingInfoResp {

	private int code;
	private DataInner data;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class DataInner {
		private List<AcceptedItem> accepted;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class AcceptedItem {
		private int carrier;
		private String number;
		@JsonProperty("track_info")
		private TrackInfo trackInfo;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class TrackInfo {
		@JsonProperty("latest_status")
		private Status latestStatus;
		private Tracking tracking;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Status {
		private String status;
		private String subStatus;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Tracking {
		private List<Provider> providers;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Provider {
		private List<Event> events;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Event {
		private String description;
		private String location;
		@JsonProperty("time_utc")
		private String timeUtc;
		@JsonProperty("sub_status")
		private String subStatus;
	}
}
