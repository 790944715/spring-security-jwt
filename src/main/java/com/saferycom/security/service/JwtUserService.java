package com.saferycom.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.saferycom.cache.service.Cache;
import com.saferycom.rbac.user.model.User;
import com.saferycom.rbac.user.service.IUserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;

@Component
public class JwtUserService implements UserDetailsService {
    private final IUserService userService;
    private final Cache cache;

    public JwtUserService(IUserService userService,
                          Cache cache) {
        this.userService = userService;
        this.cache = cache;
    }

    public String getToken(UserDetails user) {
        long timeout = 60 * 60 * 1000; // 设置1小时后过期
        String salt = BCrypt.gensalt();
        Algorithm algorithm = Algorithm.HMAC256(salt);
        String token = JWT.create()
                .withSubject(user.getUsername())    // 对应数据库中的ID
                .withExpiresAt(new Date(System.currentTimeMillis() + timeout)) //设置1小时后过期
                .withIssuedAt(new Date())
                .sign(algorithm);
        cache.set(user.getUsername(),
                buildUserDetails(user.getUsername(), salt, user.getAuthorities()),
                timeout); // 存入userDetials信息
        return token;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userService.findUserById(s);
        //真实系统需要从数据库或缓存中获取，这里对密码做了加密
        return buildUserDetails(user.getId(), user.getPassword(), user.getRole());
    }

    public static UserDetails buildUserDetails(String username, String password, String... roles) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(password)
                .roles(roles).build();
    }

    public static UserDetails buildUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(password)
                .authorities(authorities).build();
    }
}
