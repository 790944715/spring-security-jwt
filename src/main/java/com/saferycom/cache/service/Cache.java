package com.saferycom.cache.service;

/**
 * 缓存接口
 */
public interface Cache {

    String CACHE_FLAG = "Cache";

    /**
     * 设置缓存
     */
    <T> void set(String key, T value, long timeout);

    /**
     * 设置缓存过期时间
     */
    <T> void setExpireAt(String key, T value, long expireAt);

    /**
     * 获取缓存
     */
    <T> T get(String key);

    /**
     * 使缓存在某个时间过期
     */
    boolean expire(String key, long timeout);

    /**
     * 使得某个缓存在某个时刻过期
     */
    boolean expireAt(String key, long expireAt);

    /**
     * 判断某个缓存是否存在
     */
    boolean exist(String key);

    /**
     * 删除缓存
     */
    boolean del(String key);
}
