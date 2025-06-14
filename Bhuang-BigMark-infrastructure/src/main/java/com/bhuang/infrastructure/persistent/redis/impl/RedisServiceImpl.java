package com.bhuang.infrastructure.persistent.redis.impl;

import com.bhuang.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis服务实现类
 * @author bhuang
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void set(String key, Object value) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value);
    }

    @Override
    public void set(String key, Object value, Duration duration) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        bucket.set(value, duration);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        RBucket<T> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    @Override
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    @Override
    public boolean expire(String key, Duration duration) {
        return redissonClient.getBucket(key).expire(duration);
    }

    @Override
    public long getExpire(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    @Override
    public long increment(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.incrementAndGet();
    }

    @Override
    public long increment(String key, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(delta);
    }

    @Override
    public long decrement(String key) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.decrementAndGet();
    }

    @Override
    public long decrement(String key, long delta) {
        RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
        return atomicLong.addAndGet(-delta);
    }

    // ========== Hash操作 ==========

    @Override
    public void hSet(String key, String hashKey, Object value) {
        RMap<String, Object> map = redissonClient.getMap(key);
        map.put(hashKey, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String hashKey) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return (T) map.get(hashKey);
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.readAllMap();
    }

    @Override
    public void hSetAll(String key, Map<String, Object> hashMap) {
        RMap<String, Object> map = redissonClient.getMap(key);
        map.putAll(hashMap);
    }

    @Override
    public long hDelete(String key, String... hashKeys) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.fastRemove(hashKeys);
    }

    @Override
    public boolean hExists(String key, String hashKey) {
        RMap<String, Object> map = redissonClient.getMap(key);
        return map.containsKey(hashKey);
    }

    // ========== List操作 ==========

    @Override
    public long lPush(String key, Object value) {
        RList<Object> list = redissonClient.getList(key);
        list.add(0, value);
        return list.size();
    }

    @Override
    public long rPush(String key, Object value) {
        RList<Object> list = redissonClient.getList(key);
        list.add(value);
        return list.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lPop(String key) {
        RList<Object> list = redissonClient.getList(key);
        return (T) list.remove(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T rPop(String key) {
        RList<Object> list = redissonClient.getList(key);
        return (T) list.remove(list.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end) {
        RList<Object> list = redissonClient.getList(key);
        return (List<T>) list.range((int) start, (int) end);
    }

    @Override
    public long lSize(String key) {
        RList<Object> list = redissonClient.getList(key);
        return list.size();
    }

    // ========== Set操作 ==========

    @Override
    public long sAdd(String key, Object... values) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.addAll(Arrays.asList(values)) ? values.length : 0;
    }

    @Override
    public long sRemove(String key, Object... values) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.removeAll(Arrays.asList(values)) ? values.length : 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> sMembers(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return (Set<T>) set.readAll();
    }

    @Override
    public boolean sIsMember(String key, Object value) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.contains(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T sRandomMember(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return (T) set.random();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> sRandomMembers(String key, long count) {
        RSet<Object> set = redissonClient.getSet(key);
        return (List<T>) set.random((int) count);
    }

    @Override
    public long sSize(String key) {
        RSet<Object> set = redissonClient.getSet(key);
        return set.size();
    }

} 