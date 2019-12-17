package com.saferycom.security.handle;

import com.alibaba.fastjson.JSONObject;
import com.saferycom.bean.AztResponse;
import com.saferycom.util.enumData.AztResultEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录失败处理
 */
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        AuthenticationException e) throws IOException {
        logger.info("登录失败:{}", e.getMessage());
        httpServletResponse.setStatus(HttpStatus.OK.value());
        AztResponse response = new AztResponse();
        response.setResult(AztResultEnum.FAIL.getIndex());
        response.setMsg("用户名或密码错误");
        // 发送响应
        httpServletResponse.setContentType("application/json");
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.getWriter().write(JSONObject.toJSONString(response));
        httpServletResponse.getWriter().flush();
    }
}
