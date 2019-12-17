package com.saferycom.security.handle;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.saferycom.security.bean.JwtAuthenticationToken;
import com.saferycom.security.service.JwtUserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.Date;

/**
 * 验证TOKEN通过后自动刷新
 */
@Component
public class JwtRefreshSuccessHandler implements AuthenticationSuccessHandler {
    private static final int tokenRefreshInterval = 30 * 60 * 1000;  //刷新间隔30分钟
    private final JwtUserService jwtUserService;

    public JwtRefreshSuccessHandler(JwtUserService jwtUserService) {
        this.jwtUserService = jwtUserService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        DecodedJWT jwt = ((JwtAuthenticationToken) authentication).getToken();
        boolean shouldRefresh = shouldTokenRefresh(jwt.getIssuedAt());
        if (shouldRefresh) {
            String newToken = jwtUserService.getToken((UserDetails) authentication.getPrincipal());
            response.setHeader("Authorization", newToken);
        }
    }

    private boolean shouldTokenRefresh(Date issueAt) {
        Calendar issueTime = Calendar.getInstance();
        issueTime.setTime(issueAt);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MILLISECOND, -tokenRefreshInterval);
        return now.after(issueAt);
    }

}
