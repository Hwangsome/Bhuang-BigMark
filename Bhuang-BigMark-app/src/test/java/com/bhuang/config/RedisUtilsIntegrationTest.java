package com.bhuang.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 工具类集成测试
 * 测试 Redis 各种数据结构的操作
 * @author bhuang
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisUtilsIntegrationTest {

    @Autowired
    private RedisUtils redisUtils;

    private static final String TEST_PREFIX = "test:integration:";
    private static boolean redisAvailable = false;

    @BeforeEach
    public void setUp() {
        // 检查 Redis 是否可用
        try {
            String testKey = TEST_PREFIX + "availability";
            redisAvailable = redisUtils.set(testKey, "test");
            if (redisAvailable) {
                redisUtils.delete(testKey);
            }
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis 服务不可用，跳过实际连接测试: {}", e.getMessage());
        }
    }

    @Test
    @Order(1)
    public void testStringOperations() {
        log.info("测试 Redis String 操作");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 String 操作测试");
            return;
        }

        String key = TEST_PREFIX + "string:test";
        String value = "Hello Redis!";

        try {
            // 测试设置和获取
            assertTrue(redisUtils.set(key, value), "设置 String 值应该成功");
            assertEquals(value, redisUtils.get(key), "获取的值应该与设置的值相同");

            // 测试带过期时间的设置
            String expireKey = TEST_PREFIX + "string:expire";
            assertTrue(redisUtils.set(expireKey, "expire_value", 2), "设置带过期时间的值应该成功");
            
            // 验证过期时间
            long expireTime = redisUtils.getExpire(expireKey);
            assertTrue(expireTime > 0 && expireTime <= 2, "过期时间应该在预期范围内");

            // 测试键是否存在
            assertTrue(redisUtils.hasKey(key), "设置的键应该存在");
            assertTrue(redisUtils.hasKey(expireKey), "设置的过期键应该存在");

            // 清理
            assertTrue(redisUtils.delete(key), "删除键应该成功");
            assertTrue(redisUtils.delete(expireKey), "删除过期键应该成功");
            
            log.info("String 操作测试完成");

        } catch (Exception e) {
            log.error("String 操作测试异常: {}", e.getMessage());
            fail("String 操作不应该抛出异常");
        }
    }

    @Test
    @Order(2)
    public void testIncrementOperations() {
        log.info("测试 Redis 递增/递减操作");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过递增操作测试");
            return;
        }

        String key = TEST_PREFIX + "incr:test";

        try {
            // 测试递增
            assertEquals(1L, redisUtils.incr(key, 1), "首次递增应该返回 1");
            assertEquals(11L, redisUtils.incr(key, 10), "第二次递增应该返回 11");

            // 测试递减
            assertEquals(6L, redisUtils.decr(key, 5), "递减后应该返回 6");
            assertEquals(1L, redisUtils.decr(key, 5), "再次递减后应该返回 1");

            // 清理
            redisUtils.delete(key);
            
            log.info("递增/递减操作测试完成");

        } catch (Exception e) {
            log.error("递增/递减操作测试异常: {}", e.getMessage());
            fail("递增/递减操作不应该抛出异常");
        }
    }

    @Test
    @Order(3)
    public void testHashOperations() {
        log.info("测试 Redis Hash 操作");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 Hash 操作测试");
            return;
        }

        String key = TEST_PREFIX + "hash:test";

        try {
            // 测试单个字段设置和获取
            assertTrue(redisUtils.hset(key, "field1", "value1"), "设置 Hash 字段应该成功");
            assertEquals("value1", redisUtils.hget(key, "field1"), "获取 Hash 字段值应该正确");

            // 测试字段是否存在
            assertTrue(redisUtils.hHasKey(key, "field1"), "Hash 字段应该存在");
            assertFalse(redisUtils.hHasKey(key, "nonexistent"), "不存在的 Hash 字段应该返回 false");

            // 测试批量设置
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("field2", "value2");
            hashMap.put("field3", "value3");
            hashMap.put("field4", 123);
            
            assertTrue(redisUtils.hmset(key, hashMap), "批量设置 Hash 应该成功");

            // 测试获取所有字段
            Map<Object, Object> allFields = redisUtils.hmget(key);
            assertNotNull(allFields, "获取所有 Hash 字段不应该为空");
            assertTrue(allFields.size() >= 4, "Hash 应该包含至少 4 个字段");

            // 测试删除字段
            redisUtils.hdel(key, "field1", "field2");
            assertFalse(redisUtils.hHasKey(key, "field1"), "删除后字段不应该存在");
            assertFalse(redisUtils.hHasKey(key, "field2"), "删除后字段不应该存在");
            assertTrue(redisUtils.hHasKey(key, "field3"), "未删除的字段应该仍然存在");

            // 清理
            redisUtils.delete(key);
            
            log.info("Hash 操作测试完成");

        } catch (Exception e) {
            log.error("Hash 操作测试异常: {}", e.getMessage());
            fail("Hash 操作不应该抛出异常");
        }
    }

    @Test
    @Order(4)
    public void testSetOperations() {
        log.info("测试 Redis Set 操作");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 Set 操作测试");
            return;
        }

        String key = TEST_PREFIX + "set:test";

        try {
            // 测试添加元素
            long addResult = redisUtils.sSet(key, "member1", "member2", "member3");
            assertEquals(3L, addResult, "添加 3 个元素应该返回 3");

            // 测试元素是否存在
            assertTrue(redisUtils.sHasKey(key, "member1"), "添加的元素应该存在");
            assertTrue(redisUtils.sHasKey(key, "member2"), "添加的元素应该存在");
            assertFalse(redisUtils.sHasKey(key, "nonexistent"), "不存在的元素应该返回 false");

            // 测试获取所有元素
            Set<Object> allMembers = redisUtils.sGet(key);
            assertNotNull(allMembers, "获取 Set 所有元素不应该为空");
            assertEquals(3, allMembers.size(), "Set 应该包含 3 个元素");
            assertTrue(allMembers.contains("member1"), "Set 应该包含 member1");
            assertTrue(allMembers.contains("member2"), "Set 应该包含 member2");
            assertTrue(allMembers.contains("member3"), "Set 应该包含 member3");

            // 清理
            redisUtils.delete(key);
            
            log.info("Set 操作测试完成");

        } catch (Exception e) {
            log.error("Set 操作测试异常: {}", e.getMessage());
            fail("Set 操作不应该抛出异常");
        }
    }

    @Test
    @Order(5)
    public void testListOperations() {
        log.info("测试 Redis List 操作");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 List 操作测试");
            return;
        }

        String key = TEST_PREFIX + "list:test";

        try {
            // 测试添加元素
            assertTrue(redisUtils.lSet(key, "item1"), "添加 List 元素应该成功");
            assertTrue(redisUtils.lSet(key, "item2"), "添加 List 元素应该成功");
            assertTrue(redisUtils.lSet(key, "item3"), "添加 List 元素应该成功");

            // 测试获取列表长度
            long listSize = redisUtils.lGetListSize(key);
            assertEquals(3L, listSize, "List 长度应该为 3");

            // 测试按索引获取元素
            assertEquals("item1", redisUtils.lGetIndex(key, 0), "索引 0 的元素应该是 item1");
            assertEquals("item2", redisUtils.lGetIndex(key, 1), "索引 1 的元素应该是 item2");
            assertEquals("item3", redisUtils.lGetIndex(key, 2), "索引 2 的元素应该是 item3");

            // 测试获取范围内的元素
            List<Object> rangeItems = redisUtils.lGet(key, 0, 1);
            assertNotNull(rangeItems, "获取范围内元素不应该为空");
            assertEquals(2, rangeItems.size(), "范围内应该有 2 个元素");

            // 测试获取所有元素
            List<Object> allItems = redisUtils.lGet(key, 0, -1);
            assertNotNull(allItems, "获取所有元素不应该为空");
            assertEquals(3, allItems.size(), "应该有 3 个元素");

            // 清理
            redisUtils.delete(key);
            
            log.info("List 操作测试完成");

        } catch (Exception e) {
            log.error("List 操作测试异常: {}", e.getMessage());
            fail("List 操作不应该抛出异常");
        }
    }

    @Test
    @Order(6)
    public void testBatchOperations() {
        log.info("测试 Redis 批量操作");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过批量操作测试");
            return;
        }

        List<String> testKeys = Arrays.asList(
                TEST_PREFIX + "batch:key1",
                TEST_PREFIX + "batch:key2",
                TEST_PREFIX + "batch:key3"
        );

        try {
            // 设置多个键
            for (String key : testKeys) {
                assertTrue(redisUtils.set(key, "batch_value_" + key), "批量设置应该成功");
            }

            // 验证键是否存在
            for (String key : testKeys) {
                assertTrue(redisUtils.hasKey(key), "批量设置的键应该存在");
            }

            // 批量删除
            long deleteCount = redisUtils.delete(testKeys);
            assertEquals(testKeys.size(), deleteCount, "批量删除应该删除所有键");

            // 验证键已被删除
            for (String key : testKeys) {
                assertFalse(redisUtils.hasKey(key), "批量删除后键不应该存在");
            }
            
            log.info("批量操作测试完成");

        } catch (Exception e) {
            log.error("批量操作测试异常: {}", e.getMessage());
            fail("批量操作不应该抛出异常");
        }
    }

    @AfterEach
    public void tearDown() {
        if (redisAvailable) {
            try {
                // 清理可能残留的测试数据
                redisUtils.delete(Arrays.asList(
                        TEST_PREFIX + "string:test",
                        TEST_PREFIX + "string:expire",
                        TEST_PREFIX + "incr:test",
                        TEST_PREFIX + "hash:test",
                        TEST_PREFIX + "set:test",
                        TEST_PREFIX + "list:test"
                ));
            } catch (Exception e) {
                log.warn("清理测试数据时出现异常: {}", e.getMessage());
            }
        }
    }
} 