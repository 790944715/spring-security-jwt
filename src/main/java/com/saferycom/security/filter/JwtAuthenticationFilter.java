package com.saferycom.security.filter;

import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.saferycom.bean.AztResponse;
import com.saferycom.security.bean.JwtAuthenticationToken;
import com.saferycom.util.enumData.AztResultEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 带Token请求校验流程
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private RequestMatcher requestMatcher = new RequestHeaderRequestMatcher("Authorization");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // 验证码校验
        HttpSession session = request.getSession();
        String verifyCode = (String) session.getAttribute("verifyCode");
        if (request.getRequestURI().equals("/manager/login")) {
            String code = request.getParameter("verifyCode");
            if (StringUtils.isBlank(code) || !code.toUpperCase().equals(verifyCode)) {
                AztResponse aztResponse = new AztResponse();
                aztResponse.setResult(AztResultEnum.FAIL.getIndex());
                aztResponse.setMsg("验证码错误!");
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(JSONObject.toJSONString(aztResponse));
                response.getWriter().flush();
                return;
            } else {
                session.removeAttribute("verifyCode");
            }
        }
        // header没带token的，直接放过，因为部分url匿名用户也可以访问
        // 如果需要不支持匿名用户的请求没带token，这里放过也没问题，
        // 因为SecurityContext中没有认证信息(登录成功后会删除，通过token认证)
        // 后面会被权限控制模块拦截
        if (!requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        //从头中获取token并封装后提交给AuthenticationManager
        String authInfo = request.getHeader("Authorization");
        String token = StringUtils.removeStart(authInfo, "Bearer ");
        try {
            if (StringUtils.isNotBlank(token)) {
                JwtAuthenticationToken authToken = new JwtAuthenticationToken(JWT.decode(token));
                // 设置token，转发到provider进行校验
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JWTDecodeException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        filterChain.doFilter(request, response);
    }
}
