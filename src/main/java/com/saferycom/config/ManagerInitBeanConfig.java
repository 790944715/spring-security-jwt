package com.saferycom.config;

import com.saferycom.hessian.HessianCustomerScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过Placeholder进行bean的初始化，可以使用javaConfig,也可以通过ImportResouce导入xml配置
 */
public class ManagerInitBeanConfig {
    /**
     * 数据库hessian配置
     */
    @Bean(name = "hessianCustomerScanner")
    public HessianCustomerScanner getHessianCustomerScanner(@Value("${hessian.dbServiceUrl}") String dbUrl) {
        HessianCustomerScanner scanner = new HessianCustomerScanner();
        scanner.setBasePackage("com.saferycom");
        Map<String, String> urls = new HashMap<>();
        urls.put("db", dbUrl);
        scanner.setUrls(urls);
        return scanner;
    }

    /**
     * 远程文件服务消费者设置
     */
//    @Bean(name = "nfsFileInfoService")
//    public InNfsFileInfoService getNfsFileInfoService(@Value("${hessian.sfsServiceUrl}") String fsUrl)
//            throws MalformedURLException {
//        String url = fsUrl + (fsUrl.endsWith("/") ? "" : "/") + "remoting/nfsFileInfoService";
//        HessianProxyFactory proxyFactory = new HessianProxyFactory();
//        proxyFactory.setReadTimeout(15000);
//        return (InNfsFileInfoService) proxyFactory.create(InNfsFileInfoService.class, url);
//    }
}