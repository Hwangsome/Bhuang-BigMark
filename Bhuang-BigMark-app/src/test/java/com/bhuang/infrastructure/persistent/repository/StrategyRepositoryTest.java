package com.bhuang.infrastructure.persistent.repository;

import com.bhuang.Application;
import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import com.bhuang.infrastructure.persistent.constants.Constants;
import com.bhuang.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * 策略仓储测试类
 * 测试策略奖品查询的缓存机制
 * @author bhuang
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
public class StrategyRepositoryTest {

    @Autowired
    private IStrategyRepository strategyRepository;

    @Autowired
    private IRedisService redisService;

    /**
     * 测试用的策略ID
     */
    private static final Long TEST_STRATEGY_ID = 100001L;

    /**
     * 缓存Key
     */
    private String CACHE_KEY;

    @Before
    public void setUp() {
        log.info("=== 策略仓储测试初始化 ===");
        
        // 使用Constants生成缓存key
        CACHE_KEY = Constants.Redis.getStrategyAwardListKey(TEST_STRATEGY_ID);
        
        // 清理可能存在的缓存
        if (redisService.exists(CACHE_KEY)) {
            redisService.delete(CACHE_KEY);
        }
        log.info("清理缓存完成");
    }

    @Test
    public void testQueryStrategyAwardListFromDatabase() {
        log.info("=== 测试从数据库查询策略奖品列表 ===");
        
        long startTime = System.currentTimeMillis();
        List<StrategyAwardEntity> result = strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID);
        long queryTime = System.currentTimeMillis() - startTime;
        
        // 验证查询结果
        assertNotNull("查询结果不应该为null", result);
        assertFalse("结果列表不应该为空", result.isEmpty());
        
        // 验证结果内容
        for (StrategyAwardEntity entity : result) {
            assertNotNull("奖品ID不应该为null", entity.getAwardId());
            assertNotNull("奖品标题不应该为null", entity.getAwardTitle());
            assertEquals("策略ID应该匹配", TEST_STRATEGY_ID, entity.getStrategyId());
        }
        
        log.info("✓ 从数据库查询成功，数量: {}, 耗时: {}ms", result.size(), queryTime);
        
        // 验证缓存已生成
        assertTrue("缓存应该已经生成", redisService.exists(CACHE_KEY));
        log.info("✓ 缓存生成验证通过");
    }

    @Test
    public void testQueryStrategyAwardListFromCache() {
        log.info("=== 测试缓存查询性能对比 ===");
        
        // 第一次查询（从数据库，同时生成缓存）
        long startTime1 = System.currentTimeMillis();
        List<StrategyAwardEntity> firstResult = strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID);
        long firstQueryTime = System.currentTimeMillis() - startTime1;
        
        // 第二次查询（从缓存）
        long startTime2 = System.currentTimeMillis();
        List<StrategyAwardEntity> secondResult = strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID);
        long secondQueryTime = System.currentTimeMillis() - startTime2;
        
        // 验证结果一致性
        assertNotNull("第一次查询结果不应该为null", firstResult);
        assertNotNull("第二次查询结果不应该为null", secondResult);
        assertEquals("两次查询的数量应该一致", firstResult.size(), secondResult.size());
        
        // 验证数据内容一致性
        for (int i = 0; i < firstResult.size(); i++) {
            StrategyAwardEntity first = firstResult.get(i);
            StrategyAwardEntity second = secondResult.get(i);
            assertEquals("奖品ID应该一致", first.getAwardId(), second.getAwardId());
            assertEquals("奖品标题应该一致", first.getAwardTitle(), second.getAwardTitle());
            assertEquals("策略ID应该一致", first.getStrategyId(), second.getStrategyId());
        }
        
        log.info("✓ 数据库查询耗时: {}ms", firstQueryTime);
        log.info("✓ 缓存查询耗时: {}ms", secondQueryTime);
        log.info("✓ 缓存性能提升: {}%", 
                ((double)(firstQueryTime - secondQueryTime) / firstQueryTime) * 100);
        
        // 通常缓存查询应该比数据库查询快
        assertTrue("缓存查询应该更快或至少不慢于数据库查询", secondQueryTime <= firstQueryTime + 50); // 允许50ms误差
    }

    @Test
    public void testQueryNonExistentStrategy() {
        log.info("=== 测试查询不存在的策略 ===");
        
        // 使用一个绝对不存在的策略ID（超大数值）
        Long nonExistentStrategyId = 999999999L;
        List<StrategyAwardEntity> result = strategyRepository.queryStrategyAwardList(nonExistentStrategyId);
        
        // 修正：空列表不等于null，应该检查是否为空
        assertNotNull("结果不应该为null", result);
        assertEquals("不存在的策略应该返回空列表", 0, result.size());
        log.info("✓ 不存在的策略查询处理正确");
    }

    @Test
    public void testCacheExpiration() {
        log.info("=== 测试缓存功能验证 ===");
        
        // 查询一次，生成缓存
        List<StrategyAwardEntity> result = strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID);
        assertNotNull("查询结果不应该为null", result);
        assertTrue("缓存应该存在", redisService.exists(CACHE_KEY));
        
        // 手动清理缓存
        redisService.delete(CACHE_KEY);
        assertFalse("缓存应该被清理", redisService.exists(CACHE_KEY));
        
        // 再次查询，应该重新从数据库查询并缓存
        List<StrategyAwardEntity> result2 = strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID);
        assertNotNull("第二次查询结果不应该为null", result2);
        assertTrue("缓存应该重新生成", redisService.exists(CACHE_KEY));
        assertEquals("两次查询结果应该一致", result.size(), result2.size());
        
        log.info("✓ 缓存过期和重建机制验证通过");
    }
} 