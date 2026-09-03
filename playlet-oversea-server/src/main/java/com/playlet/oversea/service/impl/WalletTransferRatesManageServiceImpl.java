package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.constants.Constants;
import com.playlet.oversea.dao.wallet.WalletTransfetRatesDao;
import com.playlet.oversea.entity.wallet.WalletTransfetRatesEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.WalletTransferRatesManageService;
import com.playlet.oversea.service.support.WalletTransferService;
import com.playlet.oversea.utils.GenericityUtil;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultError;
import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端内部转账费率。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletTransferRatesManageServiceImpl extends BaseApiService implements WalletTransferRatesManageService {

	@Autowired
	private WalletTransfetRatesDao walletTransfetRatesDao;
	@Autowired
	private WalletTransferService walletTransferService;

	@Override
	@SysLogAnnotation(module = "内部转账费率", type = "GET", remark = "费率列表")
	public ResponseBase findList() {
		PageHelper.startPage(Constants.PAGENUMBER, Constants.PAGESIZE);
		List<WalletTransfetRatesEntity> list = walletTransfetRatesDao.selectList(null);
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "内部转账费率", type = "POST", remark = "新增费率")
	public ResponseBase add(@RequestBody WalletTransfetRatesEntity entity) {
		try {
			WalletTransfetRatesEntity exists = walletTransfetRatesDao.findFirst();
			if (exists != null) {
				return setResultError(I18nUtil.getMessage("base_info_exist"));
			}
			GenericityUtil.setDate(entity);
			walletTransfetRatesDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet transfer rates add failed", e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "内部转账费率", type = "POST", remark = "编辑费率")
	public ResponseBase update(@RequestBody WalletTransfetRatesEntity entity) {
		try {
			WalletTransfetRatesEntity exists = walletTransfetRatesDao.selectById(entity.getId());
			if (exists == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			GenericityUtil.updateDate(entity);
			walletTransfetRatesDao.updateById(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet transfer rates update failed id={}", entity == null ? null : entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	public ResponseBase findReading() {
		return walletTransferService.findReading();
	}

	@Override
	public ResponseBase transferReading(Double sendMoney, Long uid) {
		if (uid == null) {
			return setResultError(I18nUtil.getMessage("user.account_error"));
		}
		// 管理端试算：按 wallet_uid 查本地用户，userType/localUid 不参与余额校验路径
		return walletTransferService.transferReadingByWalletUid(uid, sendMoney == null ? null : java.math.BigDecimal.valueOf(sendMoney));
	}
}
