package com.saferycom.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.saferycom.bean.AztResponse;
import com.saferycom.security.service.JwtUserService;
import com.saferycom.util.enumData.AztResultEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功处理
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUserService userService;

    public LoginSuccessHandler(JwtUserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication) throws IOException {
        SecurityContextHolder.clearContext();   // 登录成功后清除密码登录的认证信息
        AztResponse response = new AztResponse();
        response.setResult(AztResultEnum.SUCCESS.getIndex());
        response.setMsg("登录成功!");
        // 发送响应
        String token = userService.getToken((UserDetails) authentication.getPrincipal());
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setHeader("Authorization", "Bearer " + token);
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.getWriter().write(JSONObject.toJSONString(response));
        httpServletResponse.getWriter().flush();
    }
}
