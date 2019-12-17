package com.saferycom.security.configuration;

import com.saferycom.security.filter.JwtAuthenticationFilter;
import com.saferycom.security.filter.OptionsRequestFilter;
import com.saferycom.security.handle.*;
import com.saferycom.security.service.JwtAuthenticationProvider;
import com.saferycom.security.service.JwtUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

@Configurable
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";

    private final JwtAuthenticationProvider authenticationProvider;
    private final JwtUserService userService;
    private final JwtAuthenticationFilter authenticationFilter;
    private final OptionsRequestFilter optionsRequestFilter;
    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;
    private final TokenClearLogoutHandler clearLogoutHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public WebSecurityConfig(JwtAuthenticationProvider authenticationProvider,
                             JwtUserService userService,
                             JwtAuthenticationFilter authenticationFilter,
                             OptionsRequestFilter optionsRequestFilter,
                             LoginSuccessHandler successHandler,
                             LoginFailureHandler failureHandler,
                             TokenClearLogoutHandler clearLogoutHandler,
                             JwtAccessDeniedHandler jwtAccessDeniedHandler,
                             JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.authenticationProvider = authenticationProvider;
        this.userService = userService;
        this.authenticationFilter = authenticationFilter;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.optionsRequestFilter = optionsRequestFilter;
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.clearLogoutHandler = clearLogoutHandler;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.formLogin().loginProcessingUrl("/login") // 基于表单的认证
                .usernameParameter("username").passwordParameter("password")
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .and().authorizeRequests()
                .antMatchers(
                        HttpMethod.GET,
                        "/",
                        "/*.html",
                        "/favicon.ico",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/verifyCodeImage"  // 获取验证码接口
                ).permitAll() // 允许对于网站静态资源的无授权访问
                .antMatchers("/changePassword").permitAll() // 密码修改接口
                .anyRequest().authenticated() // 除上面外的所有请求全部需要鉴权认证
                .and().cors() //支持跨域
                //添加header设置，支持跨域和ajax请求
                .and().headers().addHeaderWriter(new StaticHeadersWriter(Arrays.asList(
                new Header("Access-control-Allow-Origin", "*"),
                new Header("Access-Control-Expose-Headers", "Authorization"))))
                //拦截OPTIONS请求，直接返回header
                .and().addFilterAfter(optionsRequestFilter, CorsFilter.class)
                .csrf().disable() // 由于使用的是JWT，我们这里不需要csrf
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 基于token，所以不需要session
                .and().sessionManagement().disable() // 禁用session
                .headers().cacheControl().disable() // 禁用缓存
                //使用默认的logoutFilter URL默认就是"/logout"
                .and().logout().addLogoutHandler(clearLogoutHandler) //logout时清除token
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) //logout成功后返回200
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

        http.addFilterAfter(authenticationFilter, LogoutFilter.class)
                .exceptionHandling().accessDeniedHandler(jwtAccessDeniedHandler) // 没有权限
                .authenticationEntryPoint(jwtAuthenticationEntryPoint); // 其他异常
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(userService)
                .and().authenticationProvider(authenticationProvider);
    }

    /**
     * 配置provider
     */


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "HEAD", "OPTION"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.addExposedHeader("Authorization");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
