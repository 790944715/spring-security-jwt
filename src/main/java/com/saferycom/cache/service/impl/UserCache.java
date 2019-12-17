package com.saferycom.cache.service.impl;

import org.springframework.stereotype.Component;

/**
 * 只为了获得类名称对应配置文件与  ehcache.xml中     name = "EhcacheDefaultCache"  相同
 */
@Component
public class UserCache extends EhcacheCache {
}
