package com.playlet.oversea.security;
import com.playlet.oversea.constants.MenuConstants;
import com.playlet.oversea.dao.system.SysUserDao;
import com.playlet.oversea.entity.system.SysRoleEntity;
import com.playlet.oversea.entity.system.SysUserEntity;
import com.playlet.oversea.enums.RoleTypeEnums;
import com.playlet.oversea.enums.UserStateEnums;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.DigestUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * (身份验证提供程序)：实现 security自定义身份认证验证组件
 */
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;

    private static SysUserDao sysUserMapper;

    @SuppressWarnings("static-access")
	public CustomAuthenticationProvider(UserDetailsService userDetailsService, SysUserDao sysUserMapper) {
        this.userDetailsService = userDetailsService;
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 用户名密码校验，返回身份认证令牌
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        if (null != userDetails) {
            String encodePassword = DigestUtils.md5DigestAsHex((password).getBytes());
            if (userDetails.getPassword().equals(encodePassword)) {
                ArrayList<GrantedAuthority> authorities = getUserAuthoritiesByUsername(userName);
                return new UsernamePasswordAuthenticationToken(userName, password, authorities);
            } else if (userDetails.getPassword().equals(password)) {
                ArrayList<GrantedAuthority> authorities = getUserAuthoritiesByUsername(userName);
                return new UsernamePasswordAuthenticationToken(userName, password, authorities);
            } else {
                throw new BadCredentialsException("账号密码错误");
            }
        } else {
            throw new UsernameNotFoundException("用户不存在");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private ArrayList<GrantedAuthority> getUserAuthoritiesByUsername(String username) {
        List<String> menuCodeList = sysUserMapper.findMenuCodeByUserName(username, UserStateEnums.NORMAL.getIndex());
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<>(menuCodeList.size());
        menuCodeList.forEach(menuCode -> grantedAuthorities.add(new GrantedAuthorityImpl(MenuConstants.ROLE_PREFIX + menuCode)));
        return grantedAuthorities;
    }

    public static List<GrantedAuthorityImpl> getUserAuthorities(String username) {
        List<GrantedAuthorityImpl> menuCodeList = new ArrayList<>();
        List<String> menuList = new ArrayList<>();
        SysUserEntity sysUser = sysUserMapper.findByAcctiveState(username, UserStateEnums.NORMAL.getIndex());
        if (sysUser == null) {
            return menuCodeList;
        }
        List<SysRoleEntity> roleList = sysUserMapper.findUserRole(sysUser.getId());
        boolean temp = roleList.stream().anyMatch(p -> p.getRoleKey().equals(RoleTypeEnums.ADMIN.getValue()));
        if (temp) {
            menuList = sysUserMapper.findMenuPermsAll();
        } else {
            menuList = sysUserMapper.findMenuCodeByUserName(username, UserStateEnums.NORMAL.getIndex());
        }
        if (menuList != null && menuList.size() > 0) {
            for (int i = 0; i < menuList.size(); i++) {
                menuCodeList.add(new GrantedAuthorityImpl(MenuConstants.ROLE_PREFIX + menuList.get(i)));
            }
        }
        return menuCodeList;
    }
}
