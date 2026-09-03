package com.playlet.oversea.service.support;

import com.playlet.oversea.api.request.UsdtTopinNotifyRequest;
import com.playlet.oversea.api.response.WalletTopinAddressItemResp;
import com.playlet.oversea.api.response.Web3AddressCreateResp;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.config.UsdtTopinProperties;
import com.playlet.oversea.config.heard.LanguageContext;
import com.playlet.oversea.constants.UsdtTopinConstants;
import com.playlet.oversea.constants.WalletConstants;
import com.playlet.oversea.constants.WalletNetworkTypeConstants;
import com.playlet.oversea.constants.WalletSysConfigConstants;
import com.playlet.oversea.dao.system.SysInfoDao;
import com.playlet.oversea.dao.wallet.WalletAccountDao;
import com.playlet.oversea.dao.wallet.WalletLogDao;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.dao.wallet.WalletUsdtTopupDao;
import com.playlet.oversea.dao.wallet.WalletWeb3AddressDao;
import com.playlet.oversea.entity.system.SysInfoEntity;
import com.playlet.oversea.entity.wallet.WalletAccountEntity;
import com.playlet.oversea.entity.wallet.WalletLogEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.entity.wallet.WalletUsdtTopupEntity;
import com.playlet.oversea.entity.wallet.WalletWeb3AddressEntity;
import com.playlet.oversea.enums.NoticeStateEnums;
import com.playlet.oversea.enums.WalletLogOperateTypeEnums;
import com.playlet.oversea.enums.WalletLogStatusEnums;
import com.playlet.oversea.enums.WalletLogTradeTypeEnums;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.third.UsdtTopinClient;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.IpUtil;
import com.playlet.oversea.utils.OrderCodeFactory;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * USDT 链上充值：获取地址 + 回调入账（对齐 worldpay topinUsdtAddress）。
 */
@Slf4j
@Service
public class WalletUsdtTopinService extends BaseApiService {

	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletAccountDao walletAccountDao;
	@Autowired
	private WalletWeb3AddressDao walletWeb3AddressDao;
	@Autowired
	private WalletUsdtTopupDao walletUsdtTopupDao;
	@Autowired
	private WalletLogDao walletLogDao;
	@Autowired
	private SysInfoDao sysInfoDao;
	@Autowired
	private UsdtTopinClient usdtTopinClient;
	@Autowired
	private UsdtTopinProperties usdtTopinProperties;
	@Autowired
	private IpUtil ipUtil;

	/**
	 * 获取 USDT 充值地址列表。
	 * uid 为钱包三方 uid（wallet_uid）
	 */
	public ResponseBase getTopinAddress(String uid) {
		if (StringUtils.isEmpty(uid)) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		Long walletUid;
		try {
			walletUid = Long.parseLong(uid.trim());
		} catch (NumberFormatException e) {
			log.warn("usdt topin invalid uid={}", uid);
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		WalletUserEntity user = walletUserDao.findByWalletUid(walletUid);
		if (user == null) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		try {
			WalletWeb3AddressEntity cached = walletWeb3AddressDao.findByWalletUid(walletUid);
			if (cached != null) {
				return setResultSuccess(formatAddress(cached), I18nUtil.getMessage("base_success"));
			}
			// 创建三方充值地址
			Web3AddressCreateResp created = usdtTopinClient.createAccount(uid.trim(), user.getEmail());
			WalletWeb3AddressEntity saved = saveWeb3Address(user, created);
			syncTronAddressToAccount(user, saved.getTronAddress());
			log.info("usdt topin address assigned walletUid={}", walletUid);
			return setResultSuccess(formatAddress(saved), I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("usdt topin address failed walletUid={}", walletUid, e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("usdt topin address error walletUid={}", walletUid, e);
			return setResultError(I18nUtil.getMessage("wallet.usdt_address_failed"));
		}
	}

	/** USDT 充值回调：验签 + 幂等 + 增加可用余额 + 记钱包账变 */
	@Transactional(rollbackFor = Exception.class)
	public ResponseBase handleNotify(UsdtTopinNotifyRequest body, HttpServletRequest request) {
		if (body == null || StringUtils.isEmpty(body.getHash()) || StringUtils.isEmpty(body.getAmount())) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String txHash = body.getHash().trim();
		// 非转入类型直接忽略（对齐 onetoken topinUsdtNotify）
		if (!StringUtils.isEmpty(body.getType())
				&& !UsdtTopinConstants.NOTIFY_TYPE_IN.equalsIgnoreCase(body.getType().trim())) {
			log.info("usdt topin notify skip non-in type={} hash={}", body.getType(), txHash);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		if (!StringUtils.isEmpty(body.getCoin()) && !isSupportedTopinCoin(body.getCoin())) {
			log.info("usdt topin notify skip unsupported coin={} hash={}", body.getCoin(), txHash);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		}
		String clientIp = ipUtil.getClientIp(request);
		if (!isCallbackIpAllowed(clientIp)) {
			log.warn("usdt topin notify ip denied ip={} hash={}", clientIp, txHash);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		String sign = body.getSign();
		body.setSign(null);
		if (!usdtTopinClient.verifySign(body, sign)) {
			log.warn("usdt topin notify sign failed hash={} uid={}", txHash, body.getUid());
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletUsdtTopupEntity existed = walletUsdtTopupDao.findByTxHash(txHash);
		if (existed != null) {
			log.info("usdt topin notify duplicate hash={}", txHash);
			return setResultError(I18nUtil.getMessage("wallet.usdt_topup_duplicate"));
		}
		if (walletLogDao.findByOutOrderNo(txHash) != null) {
			log.info("usdt topin notify duplicate wallet log hash={}", txHash);
			return setResultError(I18nUtil.getMessage("wallet.usdt_topup_duplicate"));
		}
		WalletUserEntity user = resolveUser(body);
		if (user == null) {
			log.warn("usdt topin notify user not found uid={} inaddress={}", body.getUid(), body.getInaddress());
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null) {
			return setResultError(I18nUtil.getMessage("wallet.not_opened"));
		}
		BigDecimal amount;
		try {
			amount = new BigDecimal(body.getAmount().trim());
		} catch (Exception e) {
			log.error("usdt topin notify invalid amount hash={} amount={}", txHash, body.getAmount(), e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		if (amount.signum() <= 0) {
			return setResultError(I18nUtil.getMessage("base_error"));
		}
		BigDecimal before = nvl(account.getAvailableBalance());
		BigDecimal after = before.add(amount);
		try {
			walletAccountDao.updateAvailableBalance(account.getId(), after);
			WalletUsdtTopupEntity row = buildTopupLog(user, account, body, amount, before, after);
			GenericityUtil.setDate(row);
			walletUsdtTopupDao.insert(row);
			insertWalletTopUpLog(user, account, body, amount, after, txHash);
		} catch (DuplicateKeyException e) {
			log.warn("usdt topin notify concurrent duplicate hash={}", txHash, e);
			return setResultError(I18nUtil.getMessage("wallet.usdt_topup_duplicate"));
		} catch (Exception e) {
			log.error("usdt topin notify persist failed hash={} walletUid={}", txHash, user.getWalletUid(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		log.info("usdt topin credited walletUid={} hash={} amount={} balanceAfter={}",
				user.getWalletUid(), txHash, amount, after);
		return setResultSuccess(I18nUtil.getMessage("base_success"));
	}

	private WalletWeb3AddressEntity saveWeb3Address(WalletUserEntity user, Web3AddressCreateResp created) {
		Date now = new Date();
		WalletWeb3AddressEntity entity = new WalletWeb3AddressEntity();
		entity.setWalletUserId(user.getId());
		entity.setWalletUid(user.getWalletUid());
		entity.setUserEmail(user.getEmail());
		entity.setTronAddress(created.getTronAddress());
		entity.setBnbAddress(created.getBnbAddress());
		entity.setEthAddress(created.getEthAddress());
		entity.setBtcAddress(created.getBtcAddress());
		entity.setSetTime(now);
		entity.setGmtModified(now);
		try {
			walletWeb3AddressDao.insert(entity);
		} catch (DuplicateKeyException e) {
			log.warn("usdt topin web3 address duplicate walletUid={}", user.getWalletUid(), e);
			WalletWeb3AddressEntity again = walletWeb3AddressDao.findByWalletUid(user.getWalletUid());
			if (again != null) {
				return again;
			}
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		} catch (Exception e) {
			log.error("usdt topin web3 address insert failed walletUid={}", user.getWalletUid(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
		return entity;
	}

	private void syncTronAddressToAccount(WalletUserEntity user, String tronAddress) {
		if (StringUtils.isEmpty(tronAddress)) {
			return;
		}
		WalletAccountEntity account = walletAccountDao.findByWalletUserId(user.getId());
		if (account == null || !StringUtils.isEmpty(account.getTronUsdtAddress())) {
			return;
		}
		walletAccountDao.updateTronUsdtAddress(account.getId(), tronAddress);
	}

	/** 组装返回列表：当前启用 TRON + BNB，与 worldpay 一致 */
	private List<WalletTopinAddressItemResp> formatAddress(WalletWeb3AddressEntity row) {
		List<WalletTopinAddressItemResp> list = new ArrayList<>();
		SysInfoEntity config = loadWalletCenterConfig();
		String countInfo = config == null ? null : config.getConfigContent();
		String countUrl = config == null ? null : config.getConfigUrl();
		if (!StringUtils.isEmpty(row.getTronAddress())) {
			list.add(buildAddressItem(row.getTronAddress(), WalletNetworkTypeConstants.TRON, countInfo, countUrl));
		}
		if (!StringUtils.isEmpty(row.getBnbAddress())) {
			list.add(buildAddressItem(row.getBnbAddress(), WalletNetworkTypeConstants.BSC, countInfo, countUrl));
		}
		return list;
	}

	private SysInfoEntity loadWalletCenterConfig() {
		String language = LanguageContext.getLanguage();
		return sysInfoDao.findContent(WalletSysConfigConstants.WALLET_CENTER_CONFIG_TYPE,
				NoticeStateEnums.NORMAL.getIndex(), language);
	}

	private static WalletTopinAddressItemResp buildAddressItem(String address, String addressType,
			String countInfo, String countUrl) {
		WalletTopinAddressItemResp item = new WalletTopinAddressItemResp();
		item.setAddress(address);
		item.setAddressType(addressType);
		item.setCountInfo(countInfo);
		item.setCountUrl(countUrl);
		return item;
	}

	private WalletUserEntity resolveUser(UsdtTopinNotifyRequest body) {
		if (!StringUtils.isEmpty(body.getUid())) {
			try {
				Long walletUid = Long.parseLong(body.getUid().trim());
				WalletUserEntity byUid = walletUserDao.findByWalletUid(walletUid);
				if (byUid != null) {
					return byUid;
				}
			} catch (NumberFormatException e) {
				log.warn("usdt topin notify invalid uid={}", body.getUid());
			}
		}
		return resolveUserByInAddress(body.getInaddress());
	}

	private WalletUserEntity resolveUserByInAddress(String inAddress) {
		if (StringUtils.isEmpty(inAddress)) {
			return null;
		}
		String address = inAddress.trim();
		WalletAccountEntity account = walletAccountDao.findByTronUsdtAddress(address);
		if (account != null && account.getWalletUserId() != null) {
			return walletUserDao.selectById(account.getWalletUserId());
		}
		WalletWeb3AddressEntity web3 = walletWeb3AddressDao.findByAnyAddress(address);
		if (web3 != null && web3.getWalletUserId() != null) {
			return walletUserDao.selectById(web3.getWalletUserId());
		}
		return null;
	}

	private WalletUsdtTopupEntity buildTopupLog(WalletUserEntity user, WalletAccountEntity account,
			UsdtTopinNotifyRequest body, BigDecimal amount, BigDecimal before, BigDecimal after) {
		WalletUsdtTopupEntity row = new WalletUsdtTopupEntity();
		row.setWalletUserId(user.getId());
		row.setWalletUid(user.getWalletUid());
		row.setUserType(user.getUserType());
		row.setLocalUid(user.getLocalUid());
		row.setTxHash(body.getHash().trim());
		row.setOrderNo(body.getOrder_no());
		row.setCoin(StringUtils.isEmpty(body.getCoin()) ? UsdtTopinConstants.COIN_USDT : body.getCoin());
		row.setAmount(amount);
		row.setOutAddress(body.getOutaddress());
		row.setInAddress(StringUtils.isEmpty(body.getInaddress()) ? account.getTronUsdtAddress() : body.getInaddress());
		row.setBalanceBefore(before);
		row.setBalanceAfter(after);
		Date now = new Date();
		row.setSetTime(now);
		row.setGmtModified(now);
		return row;
	}

	/** USDT 链上充值账变（对齐 onetoken addCallbackLog / WALLET_TOP_UP） */
	private void insertWalletTopUpLog(WalletUserEntity user, WalletAccountEntity account,
			UsdtTopinNotifyRequest body, BigDecimal amount, BigDecimal balanceAfter, String txHash) {
		Date now = new Date();
		String inAddress = StringUtils.isEmpty(body.getInaddress())
				? account.getTronUsdtAddress() : body.getInaddress();
		WalletLogEntity logEntity = new WalletLogEntity();
		logEntity.setOrderNo(StringUtils.isEmpty(body.getOrder_no())
				? OrderCodeFactory.getOrderCode(user.getWalletUid()) : body.getOrder_no());
		logEntity.setOutOrderNo(txHash);
		logEntity.setWalletUserId(user.getId());
		logEntity.setWalletUid(user.getWalletUid());
		logEntity.setTradeType(WalletLogTradeTypeEnums.INCOME.getCode());
		logEntity.setTitle(I18nUtil.getMessage("wallet.log.wallet_top_up"));
		logEntity.setNetworkType(body.getAddress());
		logEntity.setPrimevalMoney(balanceAfter);
		logEntity.setPrimevalMoneyUnit(WalletConstants.DEFAULT_CURRENCY);
		logEntity.setRealMoney(amount);
		logEntity.setServiceCharge(BigDecimal.ZERO);
		logEntity.setFormAccount(body.getOutaddress());
		logEntity.setToName(user.getEmail());
		logEntity.setToAccount(inAddress);
		logEntity.setTranHash(txHash);
		logEntity.setStatus(WalletLogStatusEnums.POSTED.getCode());
		logEntity.setOperateType(WalletLogOperateTypeEnums.WALLET_TOP_UP.getCode());
		logEntity.setSetTime(now);
		logEntity.setGmtModified(now);
		walletLogDao.insert(logEntity);
	}

	private static boolean isSupportedTopinCoin(String coin) {
		if (StringUtils.isEmpty(coin)) {
			return false;
		}
		String normalized = coin.trim();
		return UsdtTopinConstants.COIN_USDT.equalsIgnoreCase(normalized)
				|| UsdtTopinConstants.COIN_USDC.equalsIgnoreCase(normalized);
	}

	private boolean isCallbackIpAllowed(String clientIp) {
		List<String> whitelist = usdtTopinProperties.getCallbackIpWhitelist();
		if (whitelist == null || whitelist.isEmpty()) {
			return true;
		}
		return whitelist.contains(clientIp);
	}

	private static BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}
}
