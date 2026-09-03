package com.playlet.oversea.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.oversea.aop.SysLogAnnotation;
import com.playlet.oversea.base.BaseApiService;
import com.playlet.oversea.base.ResponseBase;
import com.playlet.oversea.dao.wallet.WalletTransfetContactsDao;
import com.playlet.oversea.dao.wallet.WalletTransfetListDao;
import com.playlet.oversea.dao.wallet.WalletUserDao;
import com.playlet.oversea.entity.wallet.WalletTransfetContactsEntity;
import com.playlet.oversea.entity.wallet.WalletTransfetListEntity;
import com.playlet.oversea.entity.wallet.WalletUserEntity;
import com.playlet.oversea.exception.BaseException;
import com.playlet.oversea.service.WalletTransferContactsManageService;
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
 * 管理端内部转账通讯录。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class WalletTransferContactsManageServiceImpl extends BaseApiService
		implements WalletTransferContactsManageService {

	@Autowired
	private WalletTransfetContactsDao walletTransfetContactsDao;
	@Autowired
	private WalletUserDao walletUserDao;
	@Autowired
	private WalletTransfetListDao walletTransfetListDao;

	@Override
	@SysLogAnnotation(module = "内部转账通讯录", type = "POST", remark = "新增联系人")
	public ResponseBase add(@RequestBody WalletTransfetContactsEntity entity) {
		try {
			normalizeCompatFields(entity);
			WalletUserEntity owner = walletUserDao.findByWalletUid(entity.getWalletUid());
			if (owner == null) {
				return setResultError(I18nUtil.getMessage("user.account_error"));
			}
			WalletUserEntity contact = walletUserDao.findByWalletUid(entity.getContactsWalletUid());
			if (contact == null) {
				return setResultError(I18nUtil.getMessage("user_contacts_null"));
			}
			WalletTransfetContactsEntity exists = walletTransfetContactsDao.findOne(entity.getWalletUid(),
					entity.getContactsWalletUid());
			if (exists != null) {
				return setResultError(I18nUtil.getMessage("user_contacts_not_null"));
			}
			GenericityUtil.setDate(entity);
			walletTransfetContactsDao.insert(entity);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet contacts add failed walletUid={}", entity == null ? null : entity.getWalletUid(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "内部转账通讯录", type = "GET", remark = "删除联系人")
	public ResponseBase delete(Long id) {
		try {
			WalletTransfetContactsEntity exists = walletTransfetContactsDao.selectById(id);
			if (exists == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			walletTransfetContactsDao.deleteById(id);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet contacts delete failed id={}", id, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "内部转账通讯录", type = "POST", remark = "编辑联系人")
	public ResponseBase update(@RequestBody WalletTransfetContactsEntity entity) {
		try {
			WalletTransfetContactsEntity exists = walletTransfetContactsDao.selectById(entity.getId());
			if (exists == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			exists.setContactsLabel(entity.getContactsLabel());
			GenericityUtil.updateDate(exists);
			walletTransfetContactsDao.updateById(exists);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet contacts update failed id={}", entity == null ? null : entity.getId(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "内部转账通讯录", type = "POST", remark = "联系人列表")
	public ResponseBase findList(@RequestBody WalletTransfetContactsEntity entity) {
		try {
			if (entity == null) {
				entity = new WalletTransfetContactsEntity();
			}
			normalizeCompatFields(entity);
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<WalletTransfetContactsEntity> list = walletTransfetContactsDao.findList(entity);
			return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet contacts list failed", e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "内部转账通讯录", type = "POST", remark = "最近转账")
	public ResponseBase recentTransfer(@RequestBody WalletTransfetContactsEntity entity) {
		try {
			if (entity == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			normalizeCompatFields(entity);
			WalletUserEntity owner = walletUserDao.findByWalletUid(entity.getWalletUid());
			if (owner == null) {
				return setResultError(I18nUtil.getMessage("base_error"));
			}
			PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
			List<WalletTransfetListEntity> list = walletTransfetListDao.findBySendWalletUid(entity.getWalletUid());
			if (list != null) {
				for (WalletTransfetListEntity row : list) {
					WalletTransfetContactsEntity contact = walletTransfetContactsDao.findOne(entity.getWalletUid(),
							row.getRecipientWalletUid());
					if (contact != null) {
						row.setContactsLabel(contact.getContactsLabel());
					} else {
						row.setContactsLabel("");
					}
				}
			}
			return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("wallet recent transfer failed walletUid={}", entity == null ? null : entity.getWalletUid(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private static void normalizeCompatFields(WalletTransfetContactsEntity entity) {
		if (entity.getWalletUid() == null && entity.getUid() != null) {
			entity.setWalletUid(entity.getUid());
		}
		if (entity.getContactsWalletUid() == null && entity.getContactsUid() != null) {
			entity.setContactsWalletUid(entity.getContactsUid());
		}
	}
}
