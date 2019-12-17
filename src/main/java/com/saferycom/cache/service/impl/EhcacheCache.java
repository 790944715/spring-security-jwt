package com.saferycom.cache.service.impl;

import com.saferycom.cache.service.Cache;
import com.saferycom.cache.utils.EhcacheKit;

/**
 * EhcacheCache 缓存
 */
public abstract class EhcacheCache implements Cache {
    private final EhcacheKit ehcacheKit = EhcacheKit.getInstance(this.getClass().getSimpleName());

    @Override
    public <T> T get(String key) {
        @SuppressWarnings("unchecked")
        T t = (T) ehcacheKit.get(key);
        return t;
    }

    @Override
    public <T> void set(String key, T value, long timeout) {
        ehcacheKit.put(key, value, (int) timeout);
    }

    @Override
    public boolean expire(String key, long timeout) {
        return ehcacheKit.expire(key, (int) timeout);
    }

    @Override
    public boolean exist(String key) {
        return ehcacheKit.exist(key);
    }

    @Override
    public <T> void setExpireAt(String key, T value, long expireAt) {
        ehcacheKit.put(key, value, (int) expireAt);
    }

    @Override
    public boolean expireAt(String key, long expireAt) {
        return ehcacheKit.expireAt(key, (int) expireAt);
    }

    @Override
    public boolean del(String key) {
        return ehcacheKit.remove(key);
    }

    /**
     * 清除该缓存配置方案的所有内容
     */
    public void delAll() {
        ehcacheKit.clearCache();
    }

}