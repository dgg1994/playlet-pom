package com.playlet.oversea.constants;

/**
 * worldPay 三方接口路径。
 */
public final class WalletApiPaths {

	private WalletApiPaths() {
	}

	//---------------------------------------------用户注册---------------------------------------------------------------
	/** 用户注册 */
	public static final String REGISTER_PATH = "/api/user/register";
	/** KYC国家列表 */
	public static final String COUNTRY_PATH = "/api/user/kyc/country/list";
	/** 查询KYC状态 */
	public static final String KYC_STATUS_PATH = "/api/user/kyc/status";
	/** 提交KYC信息 */
	public static final String KYC_APPLY_PATH = "/api/user/kyc/apply";
	/** KYC 证件文件上传 */
	public static final String FILE_UPLOAD_PATH = "/api/file/upload";

	//---------------------------------------------卡片-------------------------------------------------------------------
	/** 查询商户可用卡产品列表 */
	public static final String CARD_PRODUCT_LIST_PATH = "/api/bankcard/merchant/card/list";
	/** 用户卡列表 */
	public static final String CARD_USER_LIST_PATH = "/api/bankcard/user/card/list";
	/** 申请银行卡 */
	public static final String CARD_APPLY_PATH = "/api/bankcard/apply";
	/** 银行卡是否可激活 */
	public static final String CARD_CAN_ACTIVE_PATH = "/api/bankcard/get/canActive";
	/** 银行卡激活 */
	public static final String CARD_ACTIVE_PATH = "/api/bankcard/active";
	/** 设置 Pin */
	public static final String CARD_SET_PIN_PATH = "/api/bankcard/setPin";
	/** 查询银行卡余额 */
	public static final String CARD_GET_BALANCE_PATH = "/api/bankcard/getBalance";
	/** 银行卡充值 */
	public static final String CARD_RECHARGE_PATH = "/api/bankcard/recharge";
	/** 更新银行卡状态（冻结/解冻） */
	public static final String CARD_UPDATE_STATUS_PATH = "/api/bankcard/update/status";
	/** 注销银行卡 */
	public static final String CARD_CLOSE_PATH = "/api/bankcard/close";
	/** 查询银行卡信息 */
	public static final String CARD_INFO_PATH = "/api/bankcard/info";
	/** 更新银行卡邮箱 */
	public static final String CARD_UPDATE_EMAIL_PATH = "/api/bankcard/update/email";
	/** 查询 Pin */
	public static final String CARD_QUERY_PIN_PATH = "/api/bankcard/queryPin";

	//---------------------------------------------邮寄---------------------------------------------------------------
	/** 查询邮寄地区列表 */
	public static final String DELIVERY_REGION_PATH = "/api/delivery/region";
	/** 添加邮寄地址 */
	public static final String DELIVERY_ADDRESS_ADD_PATH = "/api/delivery/address/add";
	/** 更新邮寄地址 */
	public static final String DELIVERY_ADDRESS_UPDATE_PATH = "/api/delivery/address/update";
}
