package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.WalletNetworkTypeConstants;
import com.playlet.oversea.dao.wallet.WalletUsdtTopupDao;
import com.playlet.oversea.entity.wallet.WalletUsdtTopupEntity;
import com.playlet.oversea.service.WalletTopupLogManageService;
import com.playlet.oversea.utils.I18nUtil;
import com.playlet.oversea.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端链上充值日志。
 */
@Slf4j
@RestController
@CrossOrigin
public class WalletTopupLogManageServiceImpl implements WalletTopupLogManageService {

	@Autowired
	private WalletUsdtTopupDao walletUsdtTopupDao;

	@Override
	@SysLogAnnotation(module = "链上充值", type = "POST", remark = "充值日志列表")
	public ResponseBase findList(@RequestBody(required = false) WalletUsdtTopupEntity entity) {
		if (entity == null) {
			entity = new WalletUsdtTopupEntity();
		}
		if (!StringUtils.isEmpty(entity.getWalletAddressFilter())) {
			entity.setWalletAddressFilter(entity.getWalletAddressFilter().trim());
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletUsdtTopupEntity> list = walletUsdtTopupDao.findAdminList(entity);
		if (list == null) {
			list = new ArrayList<>();
		}
		for (WalletUsdtTopupEntity row : list) {
			// networkType 优先列值，否则从 out_address 推断 TRON
			if (StringUtils.isEmpty(row.getNetworkType()) && !StringUtils.isEmpty(row.getOutAddress())) {
				row.setNetworkType(WalletNetworkTypeConstants.TRON);
			}
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}
}
