package com.playlet.oversea.constants;

/**
 * 17track 物流查询 API（对齐 worldpay EmsUrlEnums）。
 */
public final class EmsTrackingConstants {

	private EmsTrackingConstants() {
	}

	public static final String REGISTER_URL = "https://api.17track.net/track/v2.2/register";

	public static final String TRACK_INFO_URL = "https://api.17track.net/track/v2.2/gettrackinfo";

	/** 17track 请求头 token 字段名 */
	public static final String API_KEY_HEADER = "17token";

	/** EMS 轨迹状态：运输中 */
	public static final String EMS_IN_TRANSIT = "InTransit";

	/** EMS 轨迹状态：配送中 */
	public static final String EMS_OUT_FOR_DELIVERY = "OutForDelivery";

	/** EMS 轨迹状态：已签收 */
	public static final String EMS_DELIVERED = "Delivered";

	/** 物流单号格式：5~50 位字母数字与中杠 */
	public static final String TRACKING_NUMBER_REGEX = "^[A-Za-z0-9-]{5,50}$";
}
