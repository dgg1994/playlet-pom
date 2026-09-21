package com.playlet.oversea.enums;

/**
 * @category 通知消息
 * @author Hlin
 *
 */
public enum MessageEnums {

	SEND_CODE_ZH(1000, "zh", "发送验证码", "您的账户有新的验证活动{}。以下是你的 BerryV 验证码。请注意，该验证码将在 10 分钟后过期，请尽快完成验证。{}BerryV"),
	// 注册成功通知
	REGISTER_ZH(1004, "zh", "注册成功通知", "您的账号{}已注册成功"),
	// 登录成功通知
	LOGIN_ZH(1005, "zh", "登录成功通知", "您的账号 {} 检测到新的登录活动，登录设备：{},登录IP：{}。请确认是否为本人操作，如非本人操作，请及时修改密码以保障账户安全"),
	/**
	 * 作家作品/剧集评审驳回通知。
	 * 占位顺序：作者名、作品名、集序号、驳回原因。
	 */
	CREATOR_AUDIT_REJECT(1006, "zh", "作品评审未通过通知",
			"尊敬的 {}：您提交的 【{}】【第{}集】 未能通过评审，原因为：“{}”。请您进行修改后再次提交评审，如对评审意见有异议也可以进行申诉。"),

	/** 飞机：KYC 失败（uid、失败原因） */
	ROBOT_KYC_ERROR_ZH(1011, "zh", "KYC认证失败通知",
			"用户：{}，KYC认证未通过审核，请前往管理端重新提交,认证失败原因：{}"),
	/** 飞机：KYC 通过（邮箱、uid） */
	ROBOT_KYC_SUCCESS_ZH(1021, "zh", "KYC认证成功发货通知",
			"用户kyc认证已通过；邮箱：{}，UID：{}，请尽快前往后台进行银行卡发货操作"),
	/** 飞机：商户余额不足（可用余额、本次需充金额） */
	ROBOT_MERCHANT_ZH(1022, "zh", "商户资产余额不足通知",
			"商户账户余额不足。当前可用余额为 {} 元，本次用户操作需充值 {} 元。请尽快完成充值避免用户充值失败。"),
	/** 飞机：商户充值到账（金额、txHash、时间） */
	ROBOT_RECHARGE_HISTORY_ZH(1023, "zh", "商户余额充值通知",
			"商户余额充值通知。充值到账金额：{}，交易哈希：{}，充值到账时间：{}。"),
	/** 飞机：链上提现待审（用户邮箱） */
	ROBOT_WITHDRAWAL_REVIEW_ZH(1036, "zh", "提现审核通知",
			"用户：{}，进行了提现申请，请及时登录后台进行操作审核。"),
	/** 飞机：KYC 提交失败（uid、原因） */
	ROBOT_KYC_NULL_ZH(1037, "zh", "KYC认证提交失败通知",
			"用户：{}，KYC认证未提交成功，请前往管理端重新提交，提交失败原因：{}");

	private Integer index;

	private String language;

	private String title;

	private String msg;

	private MessageEnums(Integer index, String language, String title, String msg) {
		this.index = index;
		this.language = language;
		this.title = title;
		this.msg = msg;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
