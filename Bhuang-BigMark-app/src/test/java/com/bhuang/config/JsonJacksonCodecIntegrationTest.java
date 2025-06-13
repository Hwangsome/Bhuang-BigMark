package com.bhuang.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * JsonJacksonCodec 集成测试
 * 测试 Redisson 的 JSON 序列化和反序列化功能
 * @author bhuang
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("dev")
public class JsonJacksonCodecIntegrationTest {

    @Autowired
    private RedissonClient redissonClient;

    private static final String TEST_PREFIX = "test:json:";
    private static boolean redisAvailable = false;

    @Before
    public void setUp() {
        // 检查 Redis 是否可用
        try {
            String testKey = TEST_PREFIX + "availability";
            redisAvailable = redissonClient.getBucket(testKey).trySet("test");
            if (redisAvailable) {
                redissonClient.getBucket(testKey).delete();
            }
        } catch (Exception e) {
            redisAvailable = false;
            log.warn("Redis 服务不可用，跳过实际连接测试: {}", e.getMessage());
        }
    }

    @Test
    public void testSimpleObjectSerialization() {
        log.info("测试简单对象的 JSON 序列化");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过简单对象序列化测试");
            return;
        }

        String key = TEST_PREFIX + "simple:object";
        SimpleTestObject original = new SimpleTestObject(
                "测试名称", 
                100, 
                BigDecimal.valueOf(99.99), 
                true
        );

        try {
            RBucket<SimpleTestObject> bucket = redissonClient.getBucket(key);
            assertTrue("简单对象设置应该成功", bucket.trySet(original));

            SimpleTestObject retrieved = bucket.get();
            assertNotNull("获取的对象不应为空", retrieved);
            assertEquals("名称应该相同", original.getName(), retrieved.getName());
            assertEquals("计数应该相同", original.getCount(), retrieved.getCount());
            assertEquals("价格应该相同", 0, original.getPrice().compareTo(retrieved.getPrice()));
            assertEquals("激活状态应该相同", original.isActive(), retrieved.isActive());

            log.info("简单对象序列化测试通过: {}", retrieved);
            bucket.delete();

        } catch (Exception e) {
            log.error("简单对象序列化测试异常: {}", e.getMessage());
            fail("简单对象序列化不应该抛出异常");
        }
    }

    @Test
    public void testComplexObjectSerialization() {
        log.info("测试复杂对象的 JSON 序列化");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过复杂对象序列化测试");
            return;
        }

        String key = TEST_PREFIX + "complex:object";
        ComplexTestObject original = createComplexTestObject();

        try {
            RBucket<ComplexTestObject> bucket = redissonClient.getBucket(key);
            assertTrue("复杂对象设置应该成功", bucket.trySet(original));

            ComplexTestObject retrieved = bucket.get();
            assertNotNull("获取的对象不应为空", retrieved);
            
            // 验证基本属性
            assertEquals("ID应该相同", original.getId(), retrieved.getId());
            assertEquals("名称应该相同", original.getName(), retrieved.getName());
            assertNotNull("创建时间不应为空", retrieved.getCreateTime());
            
            // 验证嵌套对象
            assertNotNull("地址对象不应为空", retrieved.getAddress());
            assertEquals("省份应该相同", original.getAddress().getProvince(), retrieved.getAddress().getProvince());
            assertEquals("城市应该相同", original.getAddress().getCity(), retrieved.getAddress().getCity());
            
            // 验证集合
            assertEquals("标签数量应该相同", original.getTags().size(), retrieved.getTags().size());
            assertTrue("应该包含Java标签", retrieved.getTags().contains("Java"));
            
            // 验证 Map
            assertEquals("元数据数量应该相同", original.getMetadata().size(), retrieved.getMetadata().size());
            assertEquals("部门应该相同", original.getMetadata().get("department"), retrieved.getMetadata().get("department"));

            log.info("复杂对象序列化测试通过");
            bucket.delete();

        } catch (Exception e) {
            log.error("复杂对象序列化测试异常: {}", e.getMessage());
            fail("复杂对象序列化不应该抛出异常");
        }
    }

    @Test
    public void testCollectionSerialization() {
        log.info("测试集合的 JSON 序列化");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过集合序列化测试");
            return;
        }

        String key = TEST_PREFIX + "collection";
        List<SimpleTestObject> originalList = Arrays.asList(
                new SimpleTestObject("对象1", 1, BigDecimal.valueOf(10.0), true),
                new SimpleTestObject("对象2", 2, BigDecimal.valueOf(20.0), false),
                new SimpleTestObject("对象3", 3, BigDecimal.valueOf(30.0), true)
        );

        try {
            RBucket<List<SimpleTestObject>> bucket = redissonClient.getBucket(key);
            assertTrue("集合设置应该成功", bucket.trySet(originalList));

            @SuppressWarnings("unchecked")
            List<SimpleTestObject> retrievedList = bucket.get();
            assertNotNull("获取的集合不应为空", retrievedList);
            assertEquals("集合大小应该相同", originalList.size(), retrievedList.size());
            
            for (int i = 0; i < originalList.size(); i++) {
                SimpleTestObject original = originalList.get(i);
                SimpleTestObject retrieved = retrievedList.get(i);
                assertEquals("第" + i + "个对象名称应该相同", original.getName(), retrieved.getName());
                assertEquals("第" + i + "个对象计数应该相同", original.getCount(), retrieved.getCount());
            }

            log.info("集合序列化测试通过，处理了 {} 个对象", retrievedList.size());
            bucket.delete();

        } catch (Exception e) {
            log.error("集合序列化测试异常: {}", e.getMessage());
            fail("集合序列化不应该抛出异常");
        }
    }

    @Test
    public void testRedissonRList() {
        log.info("测试 Redisson RList 的 JSON 序列化");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 RList 序列化测试");
            return;
        }

        String key = TEST_PREFIX + "rlist";

        try {
            RList<SimpleTestObject> rList = redissonClient.getList(key);
            
            // 添加对象
            rList.add(new SimpleTestObject("列表对象1", 100, BigDecimal.valueOf(100.0), true));
            rList.add(new SimpleTestObject("列表对象2", 200, BigDecimal.valueOf(200.0), false));
            
            assertEquals("RList 大小应该为 2", 2, rList.size());
            
            // 获取对象
            SimpleTestObject first = rList.get(0);
            SimpleTestObject second = rList.get(1);
            
            assertEquals("第一个对象名称应该正确", "列表对象1", first.getName());
            assertEquals("第二个对象名称应该正确", "列表对象2", second.getName());
            assertEquals("第一个对象计数应该正确", Integer.valueOf(100), first.getCount());
            assertEquals("第二个对象计数应该正确", Integer.valueOf(200), second.getCount());

            log.info("RList 序列化测试通过");
            rList.delete();

        } catch (Exception e) {
            log.error("RList 序列化测试异常: {}", e.getMessage());
            fail("RList 序列化不应该抛出异常");
        }
    }

    @Test
    public void testRedissonRMap() {
        log.info("测试 Redisson RMap 的 JSON 序列化");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 RMap 序列化测试");
            return;
        }

        String key = TEST_PREFIX + "rmap";

        try {
            RMap<String, ComplexTestObject> rMap = redissonClient.getMap(key);
            
            // 添加对象
            ComplexTestObject obj1 = createComplexTestObject();
            obj1.setName("映射对象1");
            ComplexTestObject obj2 = createComplexTestObject();
            obj2.setName("映射对象2");
            
            rMap.put("key1", obj1);
            rMap.put("key2", obj2);
            
            assertEquals("RMap 大小应该为 2", 2, rMap.size());
            assertTrue("应该包含 key1", rMap.containsKey("key1"));
            assertTrue("应该包含 key2", rMap.containsKey("key2"));
            
            // 获取对象
            ComplexTestObject retrieved1 = rMap.get("key1");
            ComplexTestObject retrieved2 = rMap.get("key2");
            
            assertNotNull("获取的对象1不应为空", retrieved1);
            assertNotNull("获取的对象2不应为空", retrieved2);
            assertEquals("对象1名称应该正确", "映射对象1", retrieved1.getName());
            assertEquals("对象2名称应该正确", "映射对象2", retrieved2.getName());

            log.info("RMap 序列化测试通过");
            rMap.delete();

        } catch (Exception e) {
            log.error("RMap 序列化测试异常: {}", e.getMessage());
            fail("RMap 序列化不应该抛出异常");
        }
    }

    @Test
    public void testNullAndEmptyValuesSerialization() {
        log.info("测试 null 和空值的序列化");
        
        if (!redisAvailable) {
            log.info("Redis 不可用，跳过 null 值序列化测试");
            return;
        }

        String key = TEST_PREFIX + "null:values";
        ComplexTestObject original = new ComplexTestObject();
        original.setId(999L);
        original.setName(null); // null 值
        original.setTags(new ArrayList<>()); // 空集合
        original.setMetadata(new HashMap<>()); // 空 Map

        try {
            RBucket<ComplexTestObject> bucket = redissonClient.getBucket(key);
            assertTrue("包含null值的对象设置应该成功", bucket.trySet(original));

            ComplexTestObject retrieved = bucket.get();
            assertNotNull("获取的对象不应为空", retrieved);
            assertEquals("ID应该相同", original.getId(), retrieved.getId());
            assertNull("名称应该为null", retrieved.getName());
            assertNotNull("标签集合不应为null", retrieved.getTags());
            assertTrue("标签集合应该为空", retrieved.getTags().isEmpty());
            assertNotNull("元数据Map不应为null", retrieved.getMetadata());
            assertTrue("元数据Map应该为空", retrieved.getMetadata().isEmpty());

            log.info("null 和空值序列化测试通过");
            bucket.delete();

        } catch (Exception e) {
            log.error("null 值序列化测试异常: {}", e.getMessage());
            fail("null 值序列化不应该抛出异常");
        }
    }

    /**
     * 创建复杂测试对象
     */
    private ComplexTestObject createComplexTestObject() {
        ComplexTestObject obj = new ComplexTestObject();
        obj.setId(1001L);
        obj.setName("复杂测试对象");
        obj.setCreateTime(LocalDateTime.now());
        
        // 嵌套对象
        Address address = new Address();
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDetail("南山区科技园");
        obj.setAddress(address);
        
        // 集合
        obj.setTags(Arrays.asList("Java", "Spring", "Redis", "JSON"));
        
        // Map
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("department", "技术部");
        metadata.put("level", "高级");
        metadata.put("salary", 20000);
        metadata.put("bonus", true);
        obj.setMetadata(metadata);
        
        return obj;
    }

    @After
    public void tearDown() {
        if (redisAvailable) {
            try {
                // 清理可能残留的测试数据
                redissonClient.getKeys().deleteByPattern(TEST_PREFIX + "*");
            } catch (Exception e) {
                log.warn("清理测试数据时出现异常: {}", e.getMessage());
            }
        }
    }

    /**
     * 简单测试对象
     */
    @Data
    public static class SimpleTestObject {
        private String name;
        private Integer count;
        private BigDecimal price;
        private boolean active;

        public SimpleTestObject() {}

        public SimpleTestObject(String name, Integer count, BigDecimal price, boolean active) {
            this.name = name;
            this.count = count;
            this.price = price;
            this.active = active;
        }
    }

    /**
     * 复杂测试对象
     */
    @Data
    public static class ComplexTestObject {
        private Long id;
        private String name;
        private LocalDateTime createTime;
        private Address address;
        private List<String> tags;
        private Map<String, Object> metadata;
    }

    /**
     * 地址对象
     */
    @Data
    public static class Address {
        private String province;
        private String city;
        private String detail;
    }
} 