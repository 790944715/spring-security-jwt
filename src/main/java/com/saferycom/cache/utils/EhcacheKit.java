package com.saferycom.cache.utils;

import com.saferycom.config.WebAppInitializer;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * ehcache 工具类
 */
public class EhcacheKit {
    private static Logger logger = LoggerFactory.getLogger(EhcacheKit.class);
    private static CacheManager manager = null;

    private Cache cache;

    private Timer timer;

    //EHCache初始化
    private static void init() {
        try {
            if (manager == null) {
                manager = CacheManager.create(EhcacheKit.class
                        .getResourceAsStream("/" + WebAppInitializer.ACTIVE_PROFILE
                                + "/ehcache.xml"));
            }
        } catch (CacheException e) {
            logger.error("初始化ehcache失败:{}", e.getMessage());
        }
    }

    /**
     * 将数据存入Cache
     *
     * @param key   类似redis的Key
     * @param value 类似redis的value，value可以是任何对象、数据类型，比如person,map,list等
     */
    public void put(String key, Object value, int seconds) {
        Element ele = new Element(key, value);
        if (seconds != 0) {//失效时的间隔时间 和最后一次操作时间间隔
            ele.setTimeToLive(seconds);
            ele.setTimeToIdle(seconds);
        }
        cache.put(ele);
    }

    /**
     * 获取缓存cachename中key对应的value
     *
     * @param key 缓存key
     * @return @see {@link Element}
     */
    public Object get(String key) {
        try {
            Element ele = cache.get(key);
            if (ele == null) {
                return null;
            }
            return ele.getObjectValue();
        } catch (IllegalStateException | CacheException e) {
            logger.error("获取缓存{}失败:{}", key, e.getMessage());
        }
        return null;
    }

    /**
     * 清除所有缓存
     */
    public void clearCache() {
        try {
            cache.removeAll();
        } catch (IllegalStateException e) {
            logger.error("清除所有缓存失败:{}", e.getMessage());
        }
    }

    /**
     * 判断缓存是否存在
     */
    public boolean exist(String key) {
        return get(key) != null;
    }

    /**
     * 移除缓存cachename中key对应的value
     */
    public boolean remove(String key) {
        boolean flag = cache.remove(key);
        cache.flush();
        return flag;
    }

    /**
     * 使缓存在某个时间过期
     */
    public boolean expire(String key, int timeout) {
        boolean boo = false;
        Element el = cache.get(key);
        if (el != null) {
            Object value = el.getObjectValue();
            put(key, value, timeout);
            boo = true;
        }
        return boo;
    }

    /**
     * 使得某个缓存在某个时刻过期
     */
    public boolean expireAt(String key, int timeout) {
        return expire(key, timeout);
    }

    /**
     * 获取实例
     */
    public static EhcacheKit getInstance(String cachename) {
        init();
        EhcacheKit instance = new EhcacheKit();
        instance.cache = manager.getCache(cachename);
        instance.timer = new Timer();
        final EhcacheKit _instance = instance;
        //默认时不会自动触发监听过期失效，只有访问才能触发，这里是为了让程序自动监听期失效
        instance.timer.schedule(new TimerTask() {//1小时监听一次过期失效
            @Override
            public void run() {
                _instance.cache.evictExpiredElements(); // 移除过期缓存
            }
        }, 500, 1000 * 30 * 60);
        return instance;
    }
}