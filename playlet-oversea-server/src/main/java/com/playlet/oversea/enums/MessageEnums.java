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
			"尊敬的 {}：您提交的 【{}】【第{}集】 未能通过评审，原因为：“{}”。请您进行修改后再次提交评审，如对评审意见有异议也可以进行申诉。");

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
