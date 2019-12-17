package com.saferycom.config;

import com.saferycom.constant.ConstData;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import org.springframework.web.util.Log4jConfigListener;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
        implements WebApplicationInitializer {
    public static String ACTIVE_PROFILE = ConstData.SPRING_PROFILES_DEFAULT;

    static {
        String acitveEnv = System.getProperty("spring.profiles.active");
        if (StringUtils.isNotBlank(acitveEnv)) {
            ACTIVE_PROFILE = acitveEnv;
        }
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebConfig.class};
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{
                new CharacterEncodingFilter("UTF-8", true),
                new DelegatingFilterProxy("springSecurityFilterChain")
        };
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // 初始化参数
        servletContext.setInitParameter("spring.profiles.default", ACTIVE_PROFILE);
        servletContext.setInitParameter("log4jConfigLocation", "classpath:" + ACTIVE_PROFILE
                + "/log4j.properties");
        servletContext.setInitParameter("webAppRootKey", "webapp.root");
        // 配置log4j
        servletContext.addListener(Log4jConfigListener.class);
        super.onStartup(servletContext);
    }
}
