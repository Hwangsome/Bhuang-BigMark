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
 * 使用Redisson客户端，支持JSON序列化，解决value乱码问题
 * @author bhuang
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void set(String key, Object value) {
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            bucket.set(value);
            log.debug("Redis设置成功，key: {}, value类型: {}", key, value != null ? value.getClass().getSimpleName() : "null");
        } catch (Exception e) {
            log.error("Redis设置失败，key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public void set(String key, Object value, Duration duration) {
        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            bucket.set(value, duration);
            log.debug("Redis设置成功（带过期时间），key: {}, 过期时间: {}秒", key, duration.getSeconds());
        } catch (Exception e) {
            log.error("Redis设置失败（带过期时间），key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            RBucket<T> bucket = redissonClient.getBucket(key);
            T value = bucket.get();
            log.debug("Redis获取成功，key: {}, value类型: {}", key, value != null ? value.getClass().getSimpleName() : "null");
            return value;
        } catch (Exception e) {
            log.error("Redis获取失败，key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try {
            RBucket<T> bucket = redissonClient.getBucket(key);
            T value = bucket.get();
            log.debug("Redis获取成功（指定类型），key: {}, 期望类型: {}", key, clazz.getSimpleName());
            return value;
        } catch (Exception e) {
            log.error("Redis获取失败（指定类型），key: {}, 期望类型: {}, 错误: {}", key, clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            boolean result = redissonClient.getBucket(key).delete();
            log.debug("Redis删除{}，key: {}", result ? "成功" : "失败（key不存在）", key);
            return result;
        } catch (Exception e) {
            log.error("Redis删除失败，key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            boolean exists = redissonClient.getBucket(key).isExists();
            log.debug("Redis检查key存在性，key: {}, 存在: {}", key, exists);
            return exists;
        } catch (Exception e) {
            log.error("Redis检查key存在性失败，key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean expire(String key, Duration duration) {
        try {
            boolean result = redissonClient.getBucket(key).expire(duration);
            log.debug("Redis设置过期时间{}，key: {}, 过期时间: {}秒", result ? "成功" : "失败", key, duration.getSeconds());
            return result;
        } catch (Exception e) {
            log.error("Redis设置过期时间失败，key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    public long getExpire(String key) {
        try {
            long remainTime = redissonClient.getBucket(key).remainTimeToLive();
            log.debug("Redis获取剩余过期时间，key: {}, 剩余时间: {}毫秒", key, remainTime);
            return remainTime;
        } catch (Exception e) {
            log.error("Redis获取剩余过期时间失败，key: {}, 错误: {}", key, e.getMessage());
            return -2; // -2表示key不存在
        }
    }

    @Override
    public long increment(String key) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long result = atomicLong.incrementAndGet();
            log.debug("Redis自增成功，key: {}, 结果: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis自增失败，key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public long increment(String key, long delta) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long result = atomicLong.addAndGet(delta);
            log.debug("Redis自增{}成功，key: {}, 结果: {}", delta, key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis自增{}失败，key: {}, 错误: {}", delta, key, e.getMessage());
            throw e;
        }
    }

    @Override
    public long decrement(String key) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long result = atomicLong.decrementAndGet();
            log.debug("Redis自减成功，key: {}, 结果: {}", key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis自减失败，key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public long decrement(String key, long delta) {
        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            long result = atomicLong.addAndGet(-delta);
            log.debug("Redis自减{}成功，key: {}, 结果: {}", delta, key, result);
            return result;
        } catch (Exception e) {
            log.error("Redis自减{}失败，key: {}, 错误: {}", delta, key, e.getMessage());
            throw e;
        }
    }

    // ========== Hash操作 ==========

    @Override
    public void hSet(String key, String hashKey, Object value) {
        try {
            RMap<String, Object> map = redissonClient.getMap(key);
            map.put(hashKey, value);
            log.debug("Redis Hash设置成功，key: {}, hashKey: {}", key, hashKey);
        } catch (Exception e) {
            log.error("Redis Hash设置失败，key: {}, hashKey: {}, 错误: {}", key, hashKey, e.getMessage());
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String hashKey) {
        try {
            RMap<String, Object> map = redissonClient.getMap(key);
            T value = (T) map.get(hashKey);
            log.debug("Redis Hash获取成功，key: {}, hashKey: {}", key, hashKey);
            return value;
        } catch (Exception e) {
            log.error("Redis Hash获取失败，key: {}, hashKey: {}, 错误: {}", key, hashKey, e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> hGetAll(String key) {
        try {
            RMap<String, Object> map = redissonClient.getMap(key);
            Map<String, Object> result = map.readAllMap();
            log.debug("Redis Hash获取全部成功，key: {}, 条目数: {}", key, result.size());
            return result;
        } catch (Exception e) {
            log.error("Redis Hash获取全部失败，key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public void hSetAll(String key, Map<String, Object> hashMap) {
        try {
            RMap<String, Object> map = redissonClient.getMap(key);
            map.putAll(hashMap);
            log.debug("Redis Hash批量设置成功，key: {}, 条目数: {}", key, hashMap.size());
        } catch (Exception e) {
            log.error("Redis Hash批量设置失败，key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public long hDelete(String key, String... hashKeys) {
        try {
            RMap<String, Object> map = redissonClient.getMap(key);
            long count = map.fastRemove(hashKeys);
            log.debug("Redis Hash删除成功，key: {}, 删除数量: {}", key, count);
            return count;
        } catch (Exception e) {
            log.error("Redis Hash删除失败，key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    @Override
    public boolean hExists(String key, String hashKey) {
        try {
            RMap<String, Object> map = redissonClient.getMap(key);
            boolean exists = map.containsKey(hashKey);
            log.debug("Redis Hash检查存在性，key: {}, hashKey: {}, 存在: {}", key, hashKey, exists);
            return exists;
        } catch (Exception e) {
            log.error("Redis Hash检查存在性失败，key: {}, hashKey: {}, 错误: {}", key, hashKey, e.getMessage());
            return false;
        }
    }

    // ========== List操作 ==========

    @Override
    public long lPush(String key, Object value) {
        try {
            RList<Object> list = redissonClient.getList(key);
            list.add(0, value);
            long size = list.size();
            log.debug("Redis List左侧推入成功，key: {}, 列表大小: {}", key, size);
            return size;
        } catch (Exception e) {
            log.error("Redis List左侧推入失败，key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    public long rPush(String key, Object value) {
        try {
            RList<Object> list = redissonClient.getList(key);
            list.add(value);
            long size = list.size();
            log.debug("Redis List右侧推入成功，key: {}, 列表大小: {}", key, size);
            return size;
        } catch (Exception e) {
            log.error("Redis List右侧推入失败，key: {}, 错误: {}", key, e.getMessage());
            throw e;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T lPop(String key) {
        try {
            RList<Object> list = redissonClient.getList(key);
            if (list.isEmpty()) {
                return null;
            }
            T value = (T) list.remove(0);
            log.debug("Redis List左侧弹出成功，key: {}", key);
            return value;
        } catch (Exception e) {
            log.error("Redis List左侧弹出失败，key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T rPop(String key) {
        try {
            RList<Object> list = redissonClient.getList(key);
            if (list.isEmpty()) {
                return null;
            }
            T value = (T) list.remove(list.size() - 1);
            log.debug("Redis List右侧弹出成功，key: {}", key);
            return value;
        } catch (Exception e) {
            log.error("Redis List右侧弹出失败，key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end) {
        try {
            RList<Object> list = redissonClient.getList(key);
            List<T> result = (List<T>) list.range((int) start, (int) end);
            log.debug("Redis List范围获取成功，key: {}, 范围: {}-{}, 结果数量: {}", key, start, end, result.size());
            return result;
        } catch (Exception e) {
            log.error("Redis List范围获取失败，key: {}, 范围: {}-{}, 错误: {}", key, start, end, e.getMessage());
            return null;
        }
    }

    @Override
    public long lSize(String key) {
        try {
            RList<Object> list = redissonClient.getList(key);
            long size = list.size();
            log.debug("Redis List大小获取成功，key: {}, 大小: {}", key, size);
            return size;
        } catch (Exception e) {
            log.error("Redis List大小获取失败，key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    // ========== Set操作 ==========

    @Override
    public long sAdd(String key, Object... values) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            long count = set.addAll(Arrays.asList(values)) ? values.length : 0;
            log.debug("Redis Set添加成功，key: {}, 添加数量: {}", key, count);
            return count;
        } catch (Exception e) {
            log.error("Redis Set添加失败，key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    @Override
    public long sRemove(String key, Object... values) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            long count = set.removeAll(Arrays.asList(values)) ? values.length : 0;
            log.debug("Redis Set删除成功，key: {}, 删除数量: {}", key, count);
            return count;
        } catch (Exception e) {
            log.error("Redis Set删除失败，key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Set<T> sMembers(String key) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            Set<T> result = (Set<T>) set.readAll();
            log.debug("Redis Set获取全部成员成功，key: {}, 成员数量: {}", key, result.size());
            return result;
        } catch (Exception e) {
            log.error("Redis Set获取全部成员失败，key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean sIsMember(String key, Object value) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            boolean isMember = set.contains(value);
            log.debug("Redis Set成员检查，key: {}, 是否存在: {}", key, isMember);
            return isMember;
        } catch (Exception e) {
            log.error("Redis Set成员检查失败，key: {}, 错误: {}", key, e.getMessage());
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T sRandomMember(String key) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            T randomMember = (T) set.random();
            log.debug("Redis Set随机获取成员成功，key: {}", key);
            return randomMember;
        } catch (Exception e) {
            log.error("Redis Set随机获取成员失败，key: {}, 错误: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> sRandomMembers(String key, long count) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            List<T> randomMembers = (List<T>) set.random((int) count);
            log.debug("Redis Set随机获取{}个成员成功，key: {}", count, key);
            return randomMembers;
        } catch (Exception e) {
            log.error("Redis Set随机获取{}个成员失败，key: {}, 错误: {}", count, key, e.getMessage());
            return null;
        }
    }

    @Override
    public long sSize(String key) {
        try {
            RSet<Object> set = redissonClient.getSet(key);
            long size = set.size();
            log.debug("Redis Set大小获取成功，key: {}, 大小: {}", key, size);
            return size;
        } catch (Exception e) {
            log.error("Redis Set大小获取失败，key: {}, 错误: {}", key, e.getMessage());
            return 0;
        }
    }
} 