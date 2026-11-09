package com.playlet.internal.service.script;

import com.playlet.internal.api.response.ScriptMarketDetailRespEntity;
import com.playlet.internal.entity.script.ScriptEntity;

import java.util.Date;

/**
 * 剧本市场公共能力：详情组装、状态文案、脱敏、审计日志。
 */
public interface ScriptSupportService {

	/**
	 * 渠道文案转换。
	 *
	 * @param code 渠道编码
	 * @return 中文文案，未知编码返回原文
	 */
	String channelText(Integer code);

	/**
	 * 授权方式文案转换。
	 *
	 * @param code 授权编码
	 * @return 中文文案，未知编码返回原文
	 */
	String licenseText(Integer code);

	/**
	 * 售卖状态文案转换。
	 *
	 * @param code 售卖状态码
	 * @return 中文文案，未知编码返回原文
	 */
	String saleStatusText(Integer code);

	/**
	 * 审核状态文案转换。
	 *
	 * @param code 审核状态码
	 * @return 中文文案，未知编码返回原文
	 */
	String auditStatusText(Integer code);

	/**
	 * 上架状态文案转换。
	 *
	 * @param code 上架状态码
	 * @return 中文文案，未知编码返回原文
	 */
	String shelfStatusText(Integer code);

	/**
	 * 剧本类型文案转换。
	 *
	 * @param code 类型编码
	 * @return 中文文案，未知编码返回原文
	 */
	String typeText(Integer code);

	/**
	 * 完结状态文案转换。
	 *
	 * @param code 完结状态码
	 * @return 中文文案，未知编码返回原文
	 */
	String finishStateText(Integer code);

	/**
	 * 申请类型文案转换。
	 *
	 * @param code 申请类型编码
	 * @return 中文文案，未知编码返回原文
	 */
	String applyTypeText(Integer code);

	/**
	 * 申请状态文案转换。
	 *
	 * @param code 申请状态编码
	 * @return 中文文案，未知编码返回原文
	 */
	String applyStatusText(Integer code);

	/**
	 * 角色类型文案转换。
	 *
	 * @param code 角色编码
	 * @return 中文文案，未知编码返回原文
	 */
	String roleText(Integer code);

	/**
	 * 发布时间文案：刚刚 / x分钟前 / x小时前 / x天前 / yyyy-MM-dd。
	 *
	 * @param time 发布时间
	 * @return 相对时间文案
	 */
	String publishedAgo(Date time);

	/**
	 * 秒转「约 m:ss」。
	 *
	 * @param seconds 时长（秒）
	 * @return 格式化为分秒文案
	 */
	String durationText(Integer seconds);

	/**
	 * 手机号脱敏展示（中间 4 位替换为 ****）。
	 *
	 * @param mobile 原始手机号
	 * @return 脱敏后的字符串，空值原样返回
	 */
	String maskMobile(String mobile);

	/**
	 * 手机号 AES 加密（用于数据库存储）。
	 *
	 * @param mobile 原始手机号
	 * @return Base64 编码的密文
	 */
	String encryptMobile(String mobile);

	/**
	 * 手机号不可逆哈希（用于检索）。
	 *
	 * @param mobile 原始手机号
	 * @return SHA-256 摘要十六进制串
	 */
	String hashMobile(String mobile);

	/**
	 * 手机号 AES 解密。
	 *
	 * @param cipher Base64 编码的密文
	 * @return 原始手机号
	 */
	String decryptMobile(String cipher);

	/**
	 * 组装剧本详情（市场详情与作家端详情共用）。
	 * <p>
	 * 负责把 {@link ScriptEntity} 填充到 {@link ScriptMarketDetailRespEntity}，
	 * 包括状态文案、发布时间、时长格式化、联系方式加密/脱敏等公共字段。
	 * </p>
	 *
	 * @param target 目标响应对象，方法内直接写入
	 * @param script 剧本实体
	 * @param uid    当前登录用户 ID，匿名访问时传 {@code null}
	 */
	void fillDetail(ScriptMarketDetailRespEntity target, ScriptEntity script, Integer uid);

	/**
	 * 写审核/上下架日志。
	 * <p>
	 * 在 {@code script_audit_log} 表插入一条操作记录，便于追溯状态变更历史。
	 * </p>
	 *
	 * @param scriptId         剧本 ID
	 * @param actionType       操作类型（审核/上架/下架等）
	 * @param fromAuditStatus  变更前审核状态
	 * @param toAuditStatus    变更后审核状态
	 * @param fromShelfStatus  变更前上架状态
	 * @param toShelfStatus    变更后上架状态
	 * @param operatorId       操作人 ID
	 * @param operatorType     操作人类型（管理员/作家）
	 * @param remark           操作备注
	 */
	void writeAuditLog(Integer scriptId,
			Integer actionType,
			Integer fromAuditStatus,
			Integer toAuditStatus,
			Integer fromShelfStatus,
			Integer toShelfStatus,
			Integer operatorId,
			Integer operatorType,
			String remark);
}
