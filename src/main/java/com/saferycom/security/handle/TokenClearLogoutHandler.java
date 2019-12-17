package com.saferycom.security.handle;

import com.saferycom.cache.service.Cache;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 清楚上下文
 */
@Component
public class TokenClearLogoutHandler implements LogoutHandler {
    private final Cache cache;

    public TokenClearLogoutHandler(Cache cache) {
        this.cache = cache;
    }

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        if (authentication == null) return;
        UserDetails user = (UserDetails) authentication.getPrincipal();
        if (user != null && user.getUsername() != null) {
            cache.del(user.getUsername());  // 清除缓存
            SecurityContextHolder.clearContext(); // 清除上下文
        }
    }
}
