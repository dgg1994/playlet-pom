package com.playlet.oversea.service.support;

import com.playlet.oversea.dao.creator.CreatorProfileDao;
import com.playlet.oversea.dao.welfare.UserWithdrawOrderDao;
import com.playlet.oversea.entity.creator.CreatorProfileEntity;
import com.playlet.oversea.enums.CreatorIdentityTypeEnums;
import com.playlet.oversea.enums.CreatorProfileAuditStatusEnums;
import com.playlet.oversea.enums.OnePayBindStatusEnums;
import com.playlet.oversea.enums.WithdrawUserTypeEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 作家资料表上的 OnePay 绑定。
 */
@Component
public class CreatorOnePayBindOps implements OnePayBindOps {

	@Autowired
	private CreatorProfileDao creatorProfileDao;
	@Autowired
	private UserWithdrawOrderDao userWithdrawOrderDao;

	@Override
	public void bind(Integer uid, String onepayAccount, String openid, Date bindTime) {
		ensureProfile(uid, bindTime);
		creatorProfileDao.updateOnePayBind(uid, onepayAccount, openid,
				OnePayBindStatusEnums.BOUND.getCode(), bindTime);
	}

	@Override
	public void unbind(Integer uid) {
		creatorProfileDao.clearOnePayBind(uid, OnePayBindStatusEnums.UNBOUND.getCode());
	}

	@Override
	public int countProcessingWithdraw(Integer uid) {
		return userWithdrawOrderDao.countProcessingByUid(uid, WithdrawUserTypeEnums.CREATOR.getCode());
	}

	/** 资料行可能缺失：先补空行再写绑定字段 */
	private void ensureProfile(Integer creatorId, Date now) {
		if (creatorProfileDao.findByCreatorId(creatorId) != null) {
			return;
		}
		CreatorProfileEntity profile = new CreatorProfileEntity();
		profile.setCreatorId(creatorId);
		profile.setIdentityType(CreatorIdentityTypeEnums.PERSONAL.getCode());
		profile.setOnepayBindStatus(OnePayBindStatusEnums.UNBOUND.getCode());
		profile.setAuditStatus(CreatorProfileAuditStatusEnums.PENDING.getCode());
		profile.setSetTime(now);
		profile.setGmtModified(now);
		creatorProfileDao.insert(profile);
	}
}
