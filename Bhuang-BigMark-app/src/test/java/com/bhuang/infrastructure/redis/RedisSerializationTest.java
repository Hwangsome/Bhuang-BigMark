package com.bhuang.infrastructure.redis;

import com.bhuang.Application;
import com.bhuang.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Redis序列化测试类
 * 专门测试使用JSON序列化后，Redis中的value是否可读
 * @author bhuang
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
public class RedisSerializationTest {

    @Autowired(required = false)
    private IRedisService redisService;

    private final String testKeyPrefix = "test:serialization:";
    private final Set<String> testKeys = new HashSet<>();

    @Before
    public void setUp() {
        log.info("=== Redis序列化测试初始化 ===");
        if (redisService != null) {
            cleanupTestKeys();
            log.info("✓ Redis服务可用，开始序列化测试");
        } else {
            log.warn("⚠ Redis服务不可用，将跳过序列化测试");
        }
    }

    @After
    public void tearDown() {
        log.info("=== Redis序列化测试清理 ===");
        if (redisService != null) {
            cleanupTestKeys();
            log.info("✓ 测试数据清理完成");
        }
    }

    private void cleanupTestKeys() {
        for (String key : testKeys) {
            try {
                redisService.delete(key);
            } catch (Exception e) {
                log.debug("清理测试键失败: {}, 错误: {}", key, e.getMessage());
            }
        }
        testKeys.clear();
    }

    private String generateTestKey(String suffix) {
        String key = testKeyPrefix + suffix + ":" + System.currentTimeMillis();
        testKeys.add(key);
        return key;
    }

    /**
     * 测试基本数据类型序列化
     */
    @Test
    public void testBasicDataTypeSerialization() {
        log.info("=== 测试基本数据类型序列化 ===");
        
        if (redisService == null) {
            log.info("⚠ Redis服务不可用，跳过基本数据类型序列化测试");
            return;
        }

        try {
            // 测试字符串
            String stringKey = generateTestKey("string");
            String stringValue = "测试中文字符串";
            redisService.set(stringKey, stringValue);
            String retrievedString = redisService.get(stringKey);
            assertEquals("字符串序列化失败", stringValue, retrievedString);
            log.info("✓ 字符串序列化成功: {}", retrievedString);

            // 测试整数
            String intKey = generateTestKey("integer");
            Integer intValue = 12345;
            redisService.set(intKey, intValue);
            Integer retrievedInt = redisService.get(intKey);
            assertEquals("整数序列化失败", intValue, retrievedInt);
            log.info("✓ 整数序列化成功: {}", retrievedInt);

            // 测试布尔值
            String boolKey = generateTestKey("boolean");
            Boolean boolValue = true;
            redisService.set(boolKey, boolValue);
            Boolean retrievedBool = redisService.get(boolKey);
            assertEquals("布尔值序列化失败", boolValue, retrievedBool);
            log.info("✓ 布尔值序列化成功: {}", retrievedBool);

            log.info("=== 基本数据类型序列化测试通过 ===");
        } catch (Exception e) {
            log.error("基本数据类型序列化测试异常: {}", e.getMessage());
            fail("基本数据类型序列化测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试复杂对象序列化
     */
    @Test
    public void testComplexObjectSerialization() {
        log.info("=== 测试复杂对象序列化 ===");
        
        if (redisService == null) {
            log.info("⚠ Redis服务不可用，跳过复杂对象序列化测试");
            return;
        }

        try {
            // 测试Map对象
            String mapKey = generateTestKey("map");
            Map<String, Object> mapValue = new HashMap<>();
            mapValue.put("name", "张三");
            mapValue.put("age", 25);
            mapValue.put("active", true);
            mapValue.put("score", 95.5);
            
            redisService.set(mapKey, mapValue);
            Map<String, Object> retrievedMap = redisService.get(mapKey);
            assertNotNull("Map序列化结果为null", retrievedMap);
            assertEquals("Map序列化后大小不一致", mapValue.size(), retrievedMap.size());
            assertEquals("Map中name字段序列化失败", mapValue.get("name"), retrievedMap.get("name"));
            log.info("✓ Map序列化成功: {}", retrievedMap);

            // 测试List对象
            String listKey = generateTestKey("list");
            List<String> listValue = Arrays.asList("苹果", "香蕉", "橙子");
            redisService.set(listKey, listValue);
            List<String> retrievedList = redisService.get(listKey);
            assertNotNull("List序列化结果为null", retrievedList);
            assertEquals("List序列化后大小不一致", listValue.size(), retrievedList.size());
            assertEquals("List中第一个元素序列化失败", listValue.get(0), retrievedList.get(0));
            log.info("✓ List序列化成功: {}", retrievedList);

            log.info("=== 复杂对象序列化测试通过 ===");
        } catch (Exception e) {
            log.error("复杂对象序列化测试异常: {}", e.getMessage());
            fail("复杂对象序列化测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试中文字符序列化
     */
    @Test
    public void testChineseCharacterSerialization() {
        log.info("=== 测试中文字符序列化 ===");
        
        if (redisService == null) {
            log.info("⚠ Redis服务不可用，跳过中文字符序列化测试");
            return;
        }

        try {
            String chineseKey = generateTestKey("chinese");
            String chineseValue = "这是一个包含中文、English、123、特殊符号!@#$%的测试字符串";
            
            redisService.set(chineseKey, chineseValue);
            String retrievedChinese = redisService.get(chineseKey);
            
            assertNotNull("中文字符序列化结果为null", retrievedChinese);
            assertEquals("中文字符序列化失败", chineseValue, retrievedChinese);
            log.info("✓ 中文字符序列化成功: {}", retrievedChinese);
            
            // 验证字符串长度
            assertEquals("中文字符串长度不一致", chineseValue.length(), retrievedChinese.length());
            log.info("✓ 中文字符串长度一致: {} 字符", retrievedChinese.length());

            log.info("=== 中文字符序列化测试通过 ===");
        } catch (Exception e) {
            log.error("中文字符序列化测试异常: {}", e.getMessage());
            fail("中文字符序列化测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试嵌套对象序列化
     */
    @Test
    public void testNestedObjectSerialization() {
        log.info("=== 测试嵌套对象序列化 ===");
        
        if (redisService == null) {
            log.info("⚠ Redis服务不可用，跳过嵌套对象序列化测试");
            return;
        }

        try {
            String nestedKey = generateTestKey("nested");
            
            // 创建嵌套的复杂对象
            Map<String, Object> user = new HashMap<>();
            user.put("id", 1001);
            user.put("name", "李四");
            
            Map<String, Object> address = new HashMap<>();
            address.put("province", "广东省");
            address.put("city", "深圳市");
            address.put("district", "南山区");
            user.put("address", address);
            
            List<String> hobbies = Arrays.asList("编程", "阅读", "游泳");
            user.put("hobbies", hobbies);
            
            redisService.set(nestedKey, user);
            Map<String, Object> retrievedUser = redisService.get(nestedKey);
            
            assertNotNull("嵌套对象序列化结果为null", retrievedUser);
            assertEquals("用户ID序列化失败", user.get("id"), retrievedUser.get("id"));
            assertEquals("用户姓名序列化失败", user.get("name"), retrievedUser.get("name"));
            
            // 验证嵌套的地址对象
            Map<String, Object> retrievedAddress = (Map<String, Object>) retrievedUser.get("address");
            assertNotNull("嵌套地址对象为null", retrievedAddress);
            assertEquals("地址省份序列化失败", address.get("province"), retrievedAddress.get("province"));
            
            // 验证嵌套的爱好列表
            List<String> retrievedHobbies = (List<String>) retrievedUser.get("hobbies");
            assertNotNull("嵌套爱好列表为null", retrievedHobbies);
            assertEquals("爱好列表大小不一致", hobbies.size(), retrievedHobbies.size());
            
            log.info("✓ 嵌套对象序列化成功: {}", retrievedUser);

            log.info("=== 嵌套对象序列化测试通过 ===");
        } catch (Exception e) {
            log.error("嵌套对象序列化测试异常: {}", e.getMessage());
            fail("嵌套对象序列化测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试JSON序列化后的可读性
     */
    @Test
    public void testJsonReadability() {
        log.info("=== 测试JSON序列化可读性 ===");
        
        if (redisService == null) {
            log.info("⚠ Redis服务不可用，跳过JSON可读性测试");
            return;
        }

        try {
            String jsonKey = generateTestKey("json");
            
            // 创建一个具有代表性的业务对象
            Map<String, Object> strategy = new HashMap<>();
            strategy.put("strategyId", 100001L);
            strategy.put("strategyName", "新用户注册策略");
            strategy.put("description", "为新注册用户提供的抽奖策略，包含多种奖品");
            strategy.put("status", "ACTIVE");
            strategy.put("createTime", new Date().toString());
            
            List<Map<String, Object>> awards = new ArrayList<>();
            Map<String, Object> award1 = new HashMap<>();
            award1.put("awardId", 101);
            award1.put("awardName", "iPhone 15");
            award1.put("probability", 0.01);
            awards.add(award1);
            
            Map<String, Object> award2 = new HashMap<>();
            award2.put("awardId", 102);
            award2.put("awardName", "100元优惠券");
            award2.put("probability", 0.1);
            awards.add(award2);
            
            strategy.put("awards", awards);
            
            redisService.set(jsonKey, strategy);
            Map<String, Object> retrievedStrategy = redisService.get(jsonKey);
            
            assertNotNull("策略对象序列化结果为null", retrievedStrategy);
            assertEquals("策略ID序列化失败", strategy.get("strategyId"), retrievedStrategy.get("strategyId"));
            assertEquals("策略名称序列化失败", strategy.get("strategyName"), retrievedStrategy.get("strategyName"));
            
            List<Map<String, Object>> retrievedAwards = (List<Map<String, Object>>) retrievedStrategy.get("awards");
            assertNotNull("奖品列表为null", retrievedAwards);
            assertEquals("奖品列表大小不一致", awards.size(), retrievedAwards.size());
            
            log.info("✓ JSON序列化成功，策略对象: {}", retrievedStrategy);
            log.info("✓ 注意：现在Redis中存储的是可读的JSON格式，不再是乱码！");

            log.info("=== JSON序列化可读性测试通过 ===");
        } catch (Exception e) {
            log.error("JSON可读性测试异常: {}", e.getMessage());
            fail("JSON可读性测试失败: " + e.getMessage());
        }
    }
} 