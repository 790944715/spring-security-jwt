package com.saferycom.security.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.saferycom.cache.service.Cache;
import com.saferycom.security.bean.JwtAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * 这个provider用来校验token
 */
@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final Cache cache;

    public JwtAuthenticationProvider(Cache cache) {
        this.cache = cache;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        DecodedJWT jwt = ((JwtAuthenticationToken) authentication).getToken();
        // 校验token是否过期
        if (jwt.getExpiresAt().before(Calendar.getInstance().getTime())) {
            throw new NonceExpiredException("无效token");
        }
        String username = jwt.getSubject();
        UserDetails user = cache.get(username);
        if (user == null || user.getPassword() == null)
            throw new NonceExpiredException("Token expires");
        String salt = user.getPassword();
        try {
            Algorithm algorithm = Algorithm.HMAC256(salt);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withSubject(username)
                    .build();
            verifier.verify(jwt.getToken());
        } catch (Exception e) {
            throw new BadCredentialsException("JWT token verify fail", e);
        }
        return new JwtAuthenticationToken(
                JwtUserService.buildUserDetails(username, salt, user.getAuthorities()),
                jwt,
                user.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(JwtAuthenticationToken.class);
    }

}
