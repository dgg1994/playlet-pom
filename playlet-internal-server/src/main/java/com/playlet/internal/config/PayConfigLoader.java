package com.playlet.internal.config;

import cn.hutool.crypto.symmetric.AES;
import com.playlet.internal.dao.pay.PaySdkConfigDao;
import com.playlet.internal.entity.pay.PaySdkConfigEntity;
import com.playlet.internal.service.support.pay.AlipaySupport;
import com.playlet.internal.service.support.pay.WechatPaySupport;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 支付配置 DB 加载器：pay_sdk_config 表有值时覆盖 application.yml 的 pay.*（空值行回落 yml 默认）。
 *
 * <p>启动时加载一次；管理端修改配置后调用 {@link #applyFromDb()} + {@link #invalidateClients()}
 * 即可热生效，无需重启。敏感项（私钥/APIv3 密钥）以 pay.config.encrypt-key 加密存储。
 */
@Slf4j
@Component
public class PayConfigLoader {

	/** 全部支持的配置键（与 application.yml pay.* 一一对应）→ 说明；管理端列表与校验共用 */
	public static final Map<String, String> KNOWN_KEYS = new LinkedHashMap<>();

	/** 加密存储的敏感键（私钥 / APIv3 密钥） */
	public static final Set<String> SECRET_KEYS = new HashSet<>(Arrays.asList(
			"pay.wechat.private-key", "pay.wechat.api-v3-key", "pay.alipay.private-key"));

	static {
		KNOWN_KEYS.put("pay.enabled", "支付总开关：true/false，关闭则现金单只落账不发三方请求");
		KNOWN_KEYS.put("pay.wechat.enabled", "微信支付开关：true/false");
		KNOWN_KEYS.put("pay.wechat.app-id", "公众号/小程序/移动应用 APPID");
		KNOWN_KEYS.put("pay.wechat.mch-id", "商户号");
		KNOWN_KEYS.put("pay.wechat.mch-serial-no", "商户 API 证书序列号");
		KNOWN_KEYS.put("pay.wechat.api-v3-key", "APIv3 密钥（加密存储）");
		KNOWN_KEYS.put("pay.wechat.private-key", "商户私钥 PEM 全文，含 -----BEGIN PRIVATE KEY-----（加密存储）");
		KNOWN_KEYS.put("pay.wechat.private-key-path", "私钥文件路径（classpath:cert/apiclient_key.pem），与私钥全文二选一，优先使用");
		KNOWN_KEYS.put("pay.wechat.notify-url", "微信支付结果通知地址，须外网可达");
		KNOWN_KEYS.put("pay.wechat.papay-plan-id", "签约计划号（商户平台「签约中心」创建，连续包月必填）");
		KNOWN_KEYS.put("pay.wechat.contract-notify-url", "微信签约/解约结果通知地址");
		KNOWN_KEYS.put("pay.wechat.deduct-notify-url", "微信代扣结果通知地址，未配置时复用 notify-url");
		KNOWN_KEYS.put("pay.wechat.refund-notify-url", "微信退款结果通知地址");
		KNOWN_KEYS.put("pay.alipay.enabled", "支付宝开关：true/false");
		KNOWN_KEYS.put("pay.alipay.app-id", "支付宝应用 APPID");
		KNOWN_KEYS.put("pay.alipay.private-key", "应用私钥 PKCS8（加密存储）");
		KNOWN_KEYS.put("pay.alipay.alipay-public-key", "支付宝公钥");
		KNOWN_KEYS.put("pay.alipay.gateway-url", "网关地址，沙箱为 https://openapi-sandbox.dl.alipaydev.com/gateway.do");
		KNOWN_KEYS.put("pay.alipay.charset", "字符集，默认 UTF-8");
		KNOWN_KEYS.put("pay.alipay.sign-type", "签名类型，默认 RSA2");
		KNOWN_KEYS.put("pay.alipay.format", "报文格式，默认 JSON");
		KNOWN_KEYS.put("pay.alipay.notify-url", "支付宝异步通知地址");
		KNOWN_KEYS.put("pay.alipay.return-url", "PC 网站支付同步跳转地址");
		KNOWN_KEYS.put("pay.alipay.timeout-express", "订单相对超时时间，默认 30m");
		KNOWN_KEYS.put("pay.alipay.sign-notify-url", "支付宝签约/解约异步通知地址");
		KNOWN_KEYS.put("pay.alipay.sign-product-code", "签约产品码：通用代扣 GENERAL_WITHHOLDING_P（勿改 CYCLE_PAY_AUTH）");
		KNOWN_KEYS.put("pay.alipay.personal-product-code", "协议产品码，与签约产品码一致");
		KNOWN_KEYS.put("pay.alipay.withhold-product-code", "代扣交易产品码：alipay.trade.create 用 GENERAL_WITHHOLDING");
	}

	@Autowired
	private PayProperties payProperties;
	@Autowired
	private PaySdkConfigDao paySdkConfigDao;
	@Autowired
	private WechatPaySupport wechatPaySupport;
	@Autowired
	private AlipaySupport alipaySupport;

	/** 敏感项加密密钥（Base64 编码的 16/24/32 字节 AES 密钥）；未配置则敏感项不允许入库 */
	@Value("${pay.config.encrypt-key:}")
	private String encryptKeyBase64;

	private volatile AES aes;

	@PostConstruct
	public void loadOnStartup() {
		try {
			int applied = applyFromDb();
			log.info("pay config applied from db, effectiveRows={}", applied);
		} catch (Exception e) {
			log.warn("pay config load from db failed, fall back to yml defaults;"
					+ " execute scripts/sql/pay_sdk_config.sql if the table is missing", e);
		}
	}

	/**
	 * 从 DB 读取全部行覆盖 PayProperties（同一实例，所有消费方立即可见）。
	 *
	 * @return 实际生效的行数；空值行与未知键跳过
	 */
	public int applyFromDb() {
		List<PaySdkConfigEntity> rows = paySdkConfigDao.findList();
		if (rows == null || rows.isEmpty()) {
			return 0;
		}
		BeanWrapper wrapper = new BeanWrapperImpl(payProperties);
		int applied = 0;
		for (PaySdkConfigEntity row : rows) {
			String key = row.getConfigKey();
			String stored = row.getConfigValue();
			if (StringUtils.isEmpty(key) || StringUtils.isEmpty(stored)) {
				continue;
			}
			String value = stored;
			if (Integer.valueOf(1).equals(row.getEncrypted())) {
				value = decrypt(stored);
			}
			String prop = toPropertyPath(key);
			if (prop == null) {
				log.warn("skip invalid pay config key: {}", key);
				continue;
			}
			try {
				wrapper.setPropertyValue(prop, value);
				applied++;
			} catch (NotWritablePropertyException e) {
				log.warn("unknown pay config key: {}", key);
			}
		}
		log.info("pay config applied from db, effectiveRows={}", applied);
		return applied;
	}

	/** 明文 → AES(Base64) 密文 */
	public String encrypt(String plain) {
		AES cipher = aes();
		if (cipher == null) {
			throw new IllegalStateException("未配置 pay.config.encrypt-key，敏感项不允许入库存储");
		}
		return cipher.encryptBase64(plain);
	}

	/** AES(Base64) 密文 → 明文 */
	public String decrypt(String cipherBase64) {
		AES cipher = aes();
		if (cipher == null) {
			throw new IllegalStateException("未配置 pay.config.encrypt-key，无法解密加密存储的支付配置");
		}
		return cipher.decryptStr(cipherBase64);
	}

	public boolean isSecretKey(String key) {
		return key != null && SECRET_KEYS.contains(key);
	}

	/** 是否已配置加密密钥（决定敏感项能否入库） */
	public boolean encryptEnabled() {
		return aes() != null;
	}

	/** 配置变更后失效微信/支付宝 SDK 客户端缓存，下次调用按新配置重建 */
	public void invalidateClients() {
		wechatPaySupport.invalidate();
		alipaySupport.invalidate();
	}

	private AES aes() {
		if (StringUtils.isEmpty(encryptKeyBase64)) {
			return null;
		}
		if (aes == null) {
			synchronized (this) {
				if (aes == null) {
					byte[] key = Base64.getDecoder().decode(encryptKeyBase64.trim());
					if (key.length != 16 && key.length != 24 && key.length != 32) {
						throw new IllegalStateException("pay.config.encrypt-key 须为 Base64 编码的 16/24/32 字节 AES 密钥");
					}
					aes = new AES(key);
				}
			}
		}
		return aes;
	}

	/** pay.wechat.app-id → wechat.appId（BeanWrapper 嵌套属性路径） */
	private String toPropertyPath(String key) {
		String rest = key.startsWith("pay.") ? key.substring(4) : null;
		if (StringUtils.isEmpty(rest)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (String seg : rest.split("\\.")) {
			if (sb.length() > 0) {
				sb.append('.');
			}
			sb.append(dashToCamel(seg));
		}
		return sb.toString();
	}

	private String dashToCamel(String seg) {
		String[] parts = seg.split("-");
		StringBuilder sb = new StringBuilder(parts[0]);
		for (int i = 1; i < parts.length; i++) {
			if (!parts[i].isEmpty()) {
				sb.append(Character.toUpperCase(parts[i].charAt(0))).append(parts[i].substring(1));
			}
		}
		return sb.toString();
	}
}
