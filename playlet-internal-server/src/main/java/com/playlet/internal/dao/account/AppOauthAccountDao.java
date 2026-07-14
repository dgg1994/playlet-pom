package com.onetoken.dao.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.onetoken.entity.account.AppOauthAccountEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface AppOauthAccountDao extends BaseMapper<AppOauthAccountEntity> {

    @Select("select * from app_oauth_account where provider = #{provider} and provider_sub = #{providerSub} limit 1")
    AppOauthAccountEntity findByProviderAndSub(@Param("provider") String provider, @Param("providerSub") String providerSub);
}
