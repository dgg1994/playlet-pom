package com.playlet.internal.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.playlet.internal.aop.SysLogAnnotation;
import com.playlet.internal.api.request.BankcardRechargeRequest;
import com.playlet.internal.api.request.WalletCardAdminUpdateRequest;
import com.playlet.internal.api.request.WalletCardShippingRequest;
import com.playlet.internal.api.response.WalletCardAdminResp;
import com.playlet.internal.base.ResponseBase;
import com.playlet.internal.constants.WalletConstants;
import com.playlet.internal.dao.wallet.WalletCardLabelDao;
import com.playlet.internal.dao.wallet.WalletCardLabelJoinDao;
import com.playlet.internal.dao.wallet.WalletCardProductDao;
import com.playlet.internal.dao.wallet.WalletCardSynopsisDao;
import com.playlet.internal.dao.wallet.WalletCardSynopsisJoinDao;
import com.playlet.internal.entity.wallet.WalletCardLabelEntity;
import com.playlet.internal.entity.wallet.WalletCardLabelJoinEntity;
import com.playlet.internal.entity.wallet.WalletCardProductEntity;
import com.playlet.internal.entity.wallet.WalletCardSynopsisEntity;
import com.playlet.internal.entity.wallet.WalletCardSynopsisJoinEntity;
import com.playlet.internal.enums.WithdrawUserTypeEnums;
import com.playlet.internal.exception.BaseException;
import com.playlet.internal.service.CardManageService;
import com.playlet.internal.service.impl.WalletCardApplyManageServiceImpl;
import com.playlet.internal.service.support.WalletAdminCardMapper;
import com.playlet.internal.service.support.WalletCardProductService;
import com.playlet.internal.service.third.WalletUserService;
import com.playlet.internal.utils.I18nUtil;
import com.playlet.internal.utils.IpUtil;
import com.playlet.internal.utils.QiniuUploadUtils;
import com.playlet.internal.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.playlet.internal.base.BaseApiService.setResultError;
import static com.playlet.internal.base.BaseApiService.setResultSuccess;

/**
 * 管理端卡产品：对齐 onetoken CardService。
 */
@Slf4j
@RestController
@CrossOrigin
@Transactional(rollbackFor = Exception.class)
public class CardManageServiceImpl implements CardManageService {

	private static final String CARD_IMG_UPLOAD_PATH = "wallet/card/";

	@Autowired
	private WalletCardProductDao walletCardProductDao;
	@Autowired
	private WalletCardProductService walletCardProductService;
	@Autowired
	private WalletCardLabelDao walletCardLabelDao;
	@Autowired
	private WalletCardLabelJoinDao walletCardLabelJoinDao;
	@Autowired
	private WalletCardSynopsisDao walletCardSynopsisDao;
	@Autowired
	private WalletCardSynopsisJoinDao walletCardSynopsisJoinDao;
	@Autowired
	private WalletUserService walletUserService;
	@Autowired
	private WalletCardApplyManageServiceImpl walletCardApplyManageService;
	@Autowired
	private WalletAdminCardMapper walletAdminCardMapper;
	@Autowired
	private IpUtil ipUtil;

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "POST", remark = "卡产品分页")
	public ResponseBase findListPag(@RequestBody(required = false) WalletCardProductEntity entity) {
		if (entity == null) {
			entity = new WalletCardProductEntity();
		}
		// onetoken 入参 bankCardNature / title
		if (!StringUtils.isEmpty(entity.getBankCardNatureFilter())) {
			entity.setBankcardNature(entity.getBankCardNatureFilter());
		}
		if (!StringUtils.isEmpty(entity.getTitle())) {
			entity.setCardTitle(entity.getTitle());
		}
		PageHelper.startPage(entity.getPageNumber(), entity.getPageSize());
		List<WalletCardProductEntity> rows = walletCardProductDao.findAdminList(entity);
		if (rows == null) {
			rows = new ArrayList<>();
		}
		walletCardProductService.enrichAdminDisplay(rows);
		List<WalletCardAdminResp> list = new ArrayList<>(rows.size());
		for (WalletCardProductEntity row : rows) {
			walletAdminCardMapper.enrichLabelAndSynopsisIds(row);
			list.add(walletAdminCardMapper.toAdminResp(row));
		}
		return setResultSuccess(new PageInfo<>(list), I18nUtil.getMessage("base_success"));
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "GET", remark = "拉取三方卡产品")
	public ResponseBase pullList() {
		try {
			return setResultSuccess(walletCardProductService.syncFromThird(), I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("card pullList failed", e);
			return setResultError(e.getMessage());
		} catch (Exception e) {
			log.error("card pullList error", e);
			return setResultError(I18nUtil.getMessage("base_error"));
		}
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "POST", remark = "编辑卡产品")
	public ResponseBase update(@RequestBody WalletCardAdminUpdateRequest req) {
		if (req == null || StringUtils.isEmpty(req.getUuid())) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		try {
			WalletCardProductEntity existed = walletCardProductDao.findByProductUuid(req.getUuid());
			if (existed == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			if (req.getTitle() != null) {
				existed.setCardTitle(req.getTitle());
			}
			if (req.getBankCardNature() != null) {
				existed.setBankcardNature(req.getBankCardNature());
			}
			if (req.getOpenCardCost() != null) {
				existed.setOpenCardCost(req.getOpenCardCost());
			}
			if (req.getPreSaveCost() != null) {
				existed.setPreSaveCost(req.getPreSaveCost());
			}
			if (req.getRechargeFee() != null) {
				existed.setRechargeFee(req.getRechargeFee());
			}
			if (req.getActiveMinLimit() != null) {
				existed.setActiveMinLimit(req.getActiveMinLimit());
			}
			if (req.getRechargeMinLimit() != null) {
				existed.setRechargeMinLimit(req.getRechargeMinLimit());
			}
			if (req.getRechargeMaxLimit() != null) {
				existed.setRechargeMaxLimit(req.getRechargeMaxLimit());
			}
			existed.setGmtModified(new Date());
			walletCardProductDao.updateById(existed);
			replaceLabelJoin(req.getUuid(), req.getLableIdList());
			replaceSynopsisJoin(req.getUuid(), req.getSynopsisIdList());
			log.info("card update success uuid={}", req.getUuid());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (BaseException e) {
			log.error("card update failed uuid={}", req.getUuid(), e);
			throw e;
		} catch (Exception e) {
			log.error("card update error uuid={}", req.getUuid(), e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "POST", remark = "修改封面图")
	public ResponseBase updateImg(String uuid, MultipartFile file) {
		return updateProductImage(uuid, file, true);
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "POST", remark = "修改列表图")
	public ResponseBase updateListImg(String uuid, MultipartFile file) {
		return updateProductImage(uuid, file, false);
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "GET", remark = "复制卡产品")
	public ResponseBase copyCard(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		try {
			WalletCardProductEntity source = walletCardProductDao.findByProductUuid(uuid);
			if (source == null) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			WalletCardProductEntity copy = new WalletCardProductEntity();
			BeanUtils.copyProperties(source, copy);
			Integer newId = walletCardProductDao.nextProductId();
			copy.setId(newId);
			copy.setProductUuid(UUID.randomUUID().toString().replace("-", ""));
			Date now = new Date();
			copy.setSetTime(now);
			copy.setGmtModified(now);
			walletCardProductDao.insert(copy);
			List<Integer> labelIds = walletCardLabelDao.queryLabelIdsByCardId(uuid);
			replaceLabelJoin(copy.getProductUuid(), labelIds);
			List<Integer> synopsisIds = walletCardSynopsisDao.querySynopsisIdsByCardId(uuid);
			replaceSynopsisJoin(copy.getProductUuid(), synopsisIds);
			log.info("card copy success sourceUuid={} newUuid={}", uuid, copy.getProductUuid());
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card copy failed uuid={}", uuid, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "GET", remark = "上下架")
	public ResponseBase upState(String uuid, Integer stateId) {
		if (StringUtils.isEmpty(uuid) || stateId == null) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		// stateId 1=上架 enable=1；2=下架 enable=0
		int enable = WalletConstants.ADMIN_CARD_STATE_ON == stateId ? 1 : 0;
		try {
			int rows = walletCardProductDao.updateEnableByProductUuid(uuid, enable);
			if (rows <= 0) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			log.info("card upState uuid={} stateId={} enable={}", uuid, stateId, enable);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card upState failed uuid={}", uuid, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "GET", remark = "删除卡产品")
	public ResponseBase delete(String uuid) {
		if (StringUtils.isEmpty(uuid)) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		try {
			walletCardLabelJoinDao.deleteByCardId(uuid);
			walletCardSynopsisJoinDao.deleteByCardId(uuid);
			walletCardProductDao.deleteByProductUuid(uuid);
			log.info("card delete uuid={}", uuid);
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card delete failed uuid={}", uuid, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "POST", remark = "管理端卡充值")
	public ResponseBase topUp(@RequestBody BankcardRechargeRequest entity) {
		if (entity == null || entity.getUid() == null) {
			// onetoken 传 uid 字符串；本地用 app_account.id
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		Integer localUid = parseLocalUid(entity.getUid());
		if (localUid == null) {
			return setResultError(I18nUtil.getMessage("user.not_null"));
		}
		entity.setType(Boolean.FALSE);
		return walletUserService.rechargeCard(WithdrawUserTypeEnums.APP.getCode(), localUid, entity);
	}

	@Override
	@SysLogAnnotation(module = "卡产品管理", type = "POST", remark = "实体卡发货")
	public ResponseBase shipping(@RequestBody WalletCardShippingRequest entity, HttpServletRequest request) {
		String clientIp = request == null ? null : ipUtil.getClientIp(request);
		return walletCardApplyManageService.shipping(entity, request);
	}

	private ResponseBase updateProductImage(String uuid, MultipartFile file, boolean cover) {
		if (StringUtils.isEmpty(uuid) || file == null || file.isEmpty()) {
			return setResultError(I18nUtil.getMessage("parameter_error"));
		}
		try {
			String url = QiniuUploadUtils.uploadFile(file, CARD_IMG_UPLOAD_PATH);
			int rows = cover
					? walletCardProductDao.updateCardImgByProductUuid(uuid, url)
					: walletCardProductDao.updateCardListImgByProductUuid(uuid, url);
			if (rows <= 0) {
				return setResultError(I18nUtil.getMessage("base_data_null"));
			}
			return setResultSuccess(I18nUtil.getMessage("base_success"));
		} catch (Exception e) {
			log.error("card update image failed uuid={} cover={}", uuid, cover, e);
			throw new BaseException(I18nUtil.getMessage("base_error"), e);
		}
	}

	private void replaceLabelJoin(String cardUuid, List<Integer> labelIdList) {
		if (labelIdList == null) {
			return;
		}
		walletCardLabelJoinDao.deleteByCardId(cardUuid);
		if (labelIdList.isEmpty()) {
			return;
		}
		Date now = new Date();
		for (Integer labelId : labelIdList) {
			if (labelId == null) {
				continue;
			}
			WalletCardLabelJoinEntity join = new WalletCardLabelJoinEntity();
			join.setCardId(cardUuid);
			join.setLabelId(labelId);
			WalletCardLabelEntity label = walletCardLabelDao.selectById(labelId);
			if (label != null) {
				join.setLanguage(label.getLanguage());
			}
			join.setSetTime(now);
			join.setGmtModified(now);
			walletCardLabelJoinDao.insert(join);
		}
	}

	private void replaceSynopsisJoin(String cardUuid, List<Integer> synopsisIdList) {
		if (synopsisIdList == null) {
			return;
		}
		walletCardSynopsisJoinDao.deleteByCardId(cardUuid);
		if (synopsisIdList.isEmpty()) {
			return;
		}
		Date now = new Date();
		for (Integer synopsisId : synopsisIdList) {
			if (synopsisId == null) {
				continue;
			}
			WalletCardSynopsisJoinEntity join = new WalletCardSynopsisJoinEntity();
			join.setCardId(cardUuid);
			join.setSynopsisId(synopsisId);
			WalletCardSynopsisEntity synopsis = walletCardSynopsisDao.selectById(synopsisId);
			if (synopsis != null) {
				join.setLanguage(synopsis.getLanguage());
			}
			join.setSetTime(now);
			join.setGmtModified(now);
			walletCardSynopsisJoinDao.insert(join);
		}
	}

	private Integer parseLocalUid(Object uid) {
		if (uid == null) {
			return null;
		}
		if (uid instanceof Integer) {
			return (Integer) uid;
		}
		try {
			return Integer.parseInt(String.valueOf(uid).trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
