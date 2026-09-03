package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletTransfetListDao;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.WalletTransferListManageService;
import com.playlet.oversea.utils.I18nUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.playlet.oversea.base.BaseApiService.setResultSuccess;

/**
 * 管理端内部转账记录。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletTransferListManageServiceImpl extends BaseApiService implements WalletTransferListManageService {

	@Autowired
	private WalletTransfetListDao walletTransfetListDao;

	@Override
	@SysLogAnnotation(module = "内部转账记录", type = "POST", remark = "转账列表")
	public ResponseBase findList(@RequestBody WalletTransfetListEntity entity) {
		try {
			if (entity == null) {
				entity = new WalletTransfetListEntity();
			}
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<WalletTransfetListEntity> list = walletTransfetListDao.findList(entity);
			return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet transfer list query failed", e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}
}
