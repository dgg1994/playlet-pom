package com.playlet.oversea.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.playlet.oversea.base.JsonData;
import com.playlet.oversea.dao.system.SysUserDao;
import com.playlet.oversea.entity.system.SysUserEntity;
import com.playlet.oversea.enums.UserStateEnums;
import com.playlet.oversea.utils.CustomUtils;

import static java.util.Collections.emptyList;
import javax.servlet.http.HttpServletResponse;


/**
 * 实现自定义用户认证接口
 *
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private SysUserDao sysUserMapper;
    
	private HttpServletResponse response;

    public UserDetailsServiceImpl(SysUserDao sysUserMapper,HttpServletResponse response) {
        this.sysUserMapper = sysUserMapper;
        this.response = response;
    }

    /**
     * 获取数据库用户信息，并封装成UserDetails.User返回
     * 这里只是验证有没有这个用户，验证密码和设置权限在CustomAuthenticationProvider中
     *
     * @param username 用户名
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUserEntity sysUser = sysUserMapper.findByAcctiveState(username,UserStateEnums.NORMAL.getIndex());
        if (sysUser == null) {
        	CustomUtils.sendJsonMessage(response, JsonData.Error("账号密码错误！！"));
        	throw new UsernameNotFoundException("账号密码错误");
        }else {
            if (sysUser.getUserState()== UserStateEnums.NORMAL.getIndex()) {
                return new User(sysUser.getUsername(), sysUser.getPassword(), emptyList());
            }else{
                CustomUtils.sendJsonMessage(response, JsonData.Error("账号已注销！！"));
                throw new UsernameNotFoundException("账号已注销！！");
            }
        }
    }

}
