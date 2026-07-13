package com.playlet.internal.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playlet.internal.base.JsonData;
import com.playlet.internal.dao.system.SysUserDao;
import com.playlet.internal.filter.GlobalRateLimitFilter;
import com.playlet.internal.filter.JWTAuthenticationFilter;
import com.playlet.internal.filter.JWTLoginFilter;
import com.playlet.internal.handler.AuthenticationLogout;
import com.playlet.internal.handler.TokenAccessDeniedHandler;
import com.playlet.internal.handler.TokenAuthenticationEntryPoint;
import com.playlet.internal.security.CustomAuthenticationProvider;
import com.playlet.internal.utils.RedisUtil;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

   private UserDetailsService userDetailsService;

   @Autowired
   RedisUtil redisUtil;

   @Autowired
   AuthenticationLogout authenticationLogout;

   @Autowired
   private SysUserDao sysUserMapper;
   
   @Autowired
   private GlobalRateLimitFilter globalRateLimitFilter;

   @Value("${dscoins.login.ipLimit.enable}")
   private Boolean ipLimit;
   
   @Value("${dscoins.login.googleLimit.enable}")
   private Boolean googleLimit;

    public WebSecurityConfig(UserDetailsService userDetailsService, SysUserDao sysUserMapper) {
        this.userDetailsService = userDetailsService;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.cors().and().csrf().disable()
		        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
		        .authorizeRequests()
                .antMatchers("/statistics/**").permitAll()
                .antMatchers("/druid/**").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/images/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/v2/api-docs").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/configuration/ui").permitAll()
                .antMatchers("/configuration/security").permitAll()
                .antMatchers( "/*.html","/**/*.html","/**/*.css", "/**/*.js","/webSocket/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .permitAll()
                .logoutSuccessHandler(authenticationLogout)
                .and()
		        .exceptionHandling().authenticationEntryPoint(new TokenAuthenticationEntryPoint())
		        .accessDeniedHandler(new TokenAccessDeniedHandler())
		        .and()
		        .addFilterBefore(globalRateLimitFilter, JWTLoginFilter.class)
                .addFilter(createJWTLoginFilter())
                .addFilter(new JWTAuthenticationFilter(authenticationManager(),redisUtil));
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new CustomAuthenticationProvider(userDetailsService, sysUserMapper));
    }

    private JWTLoginFilter createJWTLoginFilter() throws Exception {
        JWTLoginFilter filter = new JWTLoginFilter(authenticationManager(), redisUtil, sysUserMapper, googleLimit);
        filter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                                AuthenticationException exception) throws IOException {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(200);
                String msg;
                if (exception instanceof BadCredentialsException) {
                    msg = exception.getMessage();
                } else {
                    msg = "登录失败: " + exception.getMessage();
                }
                response.getWriter().write(new ObjectMapper().writeValueAsString(JsonData.Error(msg)));
            }
        });
        return filter;
    }
}
