package com.bhuang.infrastructure.persistent.redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis服务接口
 * @author bhuang
 */
public interface IRedisService {

    /**
     * 设置缓存
     *
     * @param key   键
     * @param value 值
     */
    void set(String key, Object value);

    /**
     * 设置缓存并指定过期时间
     *
     * @param key      键
     * @param value    值
     * @param duration 过期时间
     */
    void set(String key, Object value, Duration duration);

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    <T> T get(String key);

    /**
     * 获取缓存，指定类型
     *
     * @param key   键
     * @param clazz 类型
     * @return 值
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否删除成功
     */
    boolean delete(String key);

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    boolean exists(String key);

    /**
     * 设置过期时间
     *
     * @param key      键
     * @param duration 过期时间
     * @return 是否设置成功
     */
    boolean expire(String key, Duration duration);

    /**
     * 获取过期时间
     *
     * @param key 键
     * @return 过期时间（秒）
     */
    long getExpire(String key);

    /**
     * 自增
     *
     * @param key 键
     * @return 自增后的值
     */
    long increment(String key);

    /**
     * 自增指定步长
     *
     * @param key   键
     * @param delta 步长
     * @return 自增后的值
     */
    long increment(String key, long delta);

    /**
     * 自减
     *
     * @param key 键
     * @return 自减后的值
     */
    long decrement(String key);

    /**
     * 自减指定步长
     *
     * @param key   键
     * @param delta 步长
     * @return 自减后的值
     */
    long decrement(String key, long delta);

    // ========== Hash操作 ==========

    /**
     * Hash设置
     *
     * @param key     键
     * @param hashKey Hash键
     * @param value   值
     */
    void hSet(String key, String hashKey, Object value);

    /**
     * Hash获取
     *
     * @param key     键
     * @param hashKey Hash键
     * @return 值
     */
    <T> T hGet(String key, String hashKey);

    /**
     * Hash获取所有键值对
     *
     * @param key 键
     * @return Map
     */
    Map<String, Object> hGetAll(String key);

    /**
     * Hash设置多个键值对
     *
     * @param key 键
     * @param map Map
     */
    void hSetAll(String key, Map<String, Object> map);

    /**
     * Hash删除
     *
     * @param key      键
     * @param hashKeys Hash键
     * @return 删除的数量
     */
    long hDelete(String key, String... hashKeys);

    /**
     * Hash检查键是否存在
     *
     * @param key     键
     * @param hashKey Hash键
     * @return 是否存在
     */
    boolean hExists(String key, String hashKey);

    // ========== List操作 ==========

    /**
     * List左侧推入
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    long lPush(String key, Object value);

    /**
     * List右侧推入
     *
     * @param key   键
     * @param value 值
     * @return 列表长度
     */
    long rPush(String key, Object value);

    /**
     * List左侧弹出
     *
     * @param key 键
     * @return 值
     */
    <T> T lPop(String key);

    /**
     * List右侧弹出
     *
     * @param key 键
     * @return 值
     */
    <T> T rPop(String key);

    /**
     * List获取范围内的元素
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 列表
     */
    <T> List<T> lRange(String key, long start, long end);

    /**
     * List获取长度
     *
     * @param key 键
     * @return 长度
     */
    long lSize(String key);

    // ========== Set操作 ==========

    /**
     * Set添加
     *
     * @param key    键
     * @param values 值
     * @return 添加的数量
     */
    long sAdd(String key, Object... values);

    /**
     * Set删除
     *
     * @param key    键
     * @param values 值
     * @return 删除的数量
     */
    long sRemove(String key, Object... values);

    /**
     * Set获取所有成员
     *
     * @param key 键
     * @return Set
     */
    <T> Set<T> sMembers(String key);

    /**
     * Set检查成员是否存在
     *
     * @param key   键
     * @param value 值
     * @return 是否存在
     */
    boolean sIsMember(String key, Object value);

    /**
     * Set随机获取成员
     *
     * @param key 键
     * @return 随机成员
     */
    <T> T sRandomMember(String key);

    /**
     * Set随机获取多个成员
     *
     * @param key   键
     * @param count 数量
     * @return 随机成员列表
     */
    <T> List<T> sRandomMembers(String key, long count);

    /**
     * Set获取大小
     *
     * @param key 键
     * @return 大小
     */
    long sSize(String key);

} 