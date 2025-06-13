package com.bhuang.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 * 基于 Redisson 提供常用的 Redis 操作方法
 * @author bhuang
 */
@Slf4j
@Component
public class RedisUtils {
    
    private final RedissonClient redissonClient;
    
    public RedisUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }
    
    // ==================== 通用操作 ====================
    
    /**
     * 判断键是否存在
     * @param key 键
     * @return true 存在 false不存在
     */
    public boolean hasKey(String key) {
        try {
            return redissonClient.getBucket(key).isExists();
        } catch (Exception e) {
            log.error("Redis hasKey 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除键
     * @param key 键
     * @return true成功 false失败
     */
    public boolean delete(String key) {
        try {
            return redissonClient.getBucket(key).delete();
        } catch (Exception e) {
            log.error("Redis delete 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 批量删除键
     * @param keys 键集合
     * @return 删除的数量
     */
    public long delete(Collection<String> keys) {
        try {
            return redissonClient.getKeys().delete(keys.toArray(new String[0]));
        } catch (Exception e) {
            log.error("Redis delete batch 异常: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 设置过期时间
     * @param key 键
     * @param time 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redissonClient.getBucket(key).expire(time, TimeUnit.SECONDS);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Redis expire 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取过期时间
     * @param key 键
     * @return 时间(秒) 返回0代表为永久有效
     */
    public long getExpire(String key) {
        try {
            return redissonClient.getBucket(key).remainTimeToLive() / 1000;
        } catch (Exception e) {
            log.error("Redis getExpire 异常: {}", e.getMessage());
            return 0;
        }
    }
    
    // ==================== String 操作 ====================
    
    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return key == null ? null : redissonClient.getBucket(key).get();
    }
    
    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public boolean set(String key, Object value) {
        try {
            redissonClient.getBucket(key).set(value);
            return true;
        } catch (Exception e) {
            log.error("Redis set 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redissonClient.getBucket(key).set(value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis set with expire 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 递增
     * @param key 键
     * @param delta 要增加几(大于0)
     * @return 递增后的值
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redissonClient.getAtomicLong(key).addAndGet(delta);
    }
    
    /**
     * 递减
     * @param key 键
     * @param delta 要减少几(小于0)
     * @return 递减后的值
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redissonClient.getAtomicLong(key).addAndGet(-delta);
    }
    
    // ==================== Hash 操作 ====================
    
    /**
     * HashGet
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return redissonClient.getMap(key).get(item);
    }
    
    /**
     * 获取hashKey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return redissonClient.getMap(key).readAllMap();
    }
    
    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return true 成功 false 失败
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redissonClient.getMap(key).putAll(map);
            return true;
        } catch (Exception e) {
            log.error("Redis hmset 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间(秒)
     * @return true成功 false失败
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redissonClient.getMap(key).putAll(map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis hmset with expire 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 向一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true 成功 false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            redissonClient.getMap(key).put(item, value);
            return true;
        } catch (Exception e) {
            log.error("Redis hset 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除hash表中的值
     * @param key 键 不能为null
     * @param item 项 可以使多个 不能为null
     */
    public void hdel(String key, Object... item) {
        redissonClient.getMap(key).fastRemove(item);
    }
    
    /**
     * 判断hash表中是否有该项的值
     * @param key 键 不能为null
     * @param item 项 不能为null
     * @return true 存在 false不存在
     */
    public boolean hHasKey(String key, String item) {
        return redissonClient.getMap(key).containsKey(item);
    }
    
    // ==================== Set 操作 ====================
    
    /**
     * 根据key获取Set中的所有值
     * @param key 键
     * @return Set集合
     */
    public Set<Object> sGet(String key) {
        try {
            return redissonClient.getSet(key).readAll();
        } catch (Exception e) {
            log.error("Redis sGet 异常: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 根据value从一个set中查询,是否存在
     * @param key 键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redissonClient.getSet(key).contains(value);
        } catch (Exception e) {
            log.error("Redis sHasKey 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将数据放入set缓存
     * @param key 键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redissonClient.getSet(key).addAll(Arrays.asList(values)) ? values.length : 0;
        } catch (Exception e) {
            log.error("Redis sSet 异常: {}", e.getMessage());
            return 0;
        }
    }
    
    // ==================== List 操作 ====================
    
    /**
     * 获取list缓存的内容
     * @param key 键
     * @param start 开始
     * @param end 结束 0 到 -1代表所有值
     * @return List集合
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redissonClient.getList(key).range((int) start, (int) end);
        } catch (Exception e) {
            log.error("Redis lGet 异常: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取list缓存的长度
     * @param key 键
     * @return 长度
     */
    public long lGetListSize(String key) {
        try {
            return redissonClient.getList(key).size();
        } catch (Exception e) {
            log.error("Redis lGetListSize 异常: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 通过索引 获取list中的值
     * @param key 键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return 值
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redissonClient.getList(key).get((int) index);
        } catch (Exception e) {
            log.error("Redis lGetIndex 异常: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @return true 成功 false 失败
     */
    public boolean lSet(String key, Object value) {
        try {
            redissonClient.getList(key).add(value);
            return true;
        } catch (Exception e) {
            log.error("Redis lSet 异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 将list放入缓存
     * @param key 键
     * @param value 值
     * @param time 时间(秒)
     * @return true 成功 false 失败
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redissonClient.getList(key).add(value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("Redis lSet with expire 异常: {}", e.getMessage());
            return false;
        }
    }
} 