package com.playlet.internal.dao.wallet;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.playlet.internal.entity.wallet.WalletWithdrawalRatesEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 提现费率 DAO。
 */
@Repository
public interface WalletWithdrawalRatesDao extends BaseMapper<WalletWithdrawalRatesEntity> {

	@Select("select * from wallet_withdrawal_rates order by id asc")
	List<WalletWithdrawalRatesEntity> findAll();
}
