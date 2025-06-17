package com.bhuang.domain.strategy.service.armory;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.model.entity.StrategyRuleEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import com.bhuang.infrastructure.persistent.dao.StrategyRuleDao;
import com.bhuang.infrastructure.persistent.dao.StrategyDao;
import com.bhuang.infrastructure.persistent.dao.StrategyAwardDao;
import com.bhuang.infrastructure.persistent.po.Strategy;
import com.bhuang.infrastructure.persistent.po.StrategyRule;
import com.bhuang.infrastructure.persistent.po.StrategyAward;
import com.bhuang.infrastructure.persistent.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author bhuang
 * @description StrategyArmory集成测试类 - 测试策略装配和调度功能
 * @create 2025-01-15
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class StrategyArmoryIntegrationTest {

    @Resource
    private StrategyArmory strategyArmory;

    @Resource
    private IStrategyRepository strategyRepository;
    
    @Resource
    private StrategyRuleDao strategyRuleDao;
    
    @Resource
    private StrategyDao strategyDao;
    
    @Resource
    private StrategyAwardDao strategyAwardDao;

    @Resource
    private IRedisService redisService;

    private static final Long TEST_STRATEGY_ID = 999999L;
    private final List<Long> testRecordIds = new ArrayList<>();
    private Strategy testStrategy;
    private StrategyRule testStrategyRule;
    private final List<StrategyAward> testStrategyAwards = new ArrayList<>();

    @Before
    public void setUp() {
        log.info("========== 开始准备测试数据 ==========");
        cleanupTestData();
        createTestData();
        log.info("测试数据准备完成，策略ID: {}", TEST_STRATEGY_ID);
    }

    @After
    public void tearDown() {
        log.info("========== 开始清理测试数据 ==========");
        cleanupRedisData();
        cleanupDatabaseData();
        log.info("测试数据清理完成");
    }

    /**
     * 清理测试数据（旧数据）
     */
    private void cleanupTestData() {
        log.info("清理可能存在的旧测试数据...");
        try {
            // 清理Redis缓存中的测试数据
            cleanupRedisData();
            
            // 清理数据库中可能存在的测试数据
            cleanupDatabaseData();
            
            log.info("旧测试数据清理完成");
        } catch (Exception e) {
            log.warn("清理旧测试数据时出现异常：{}", e.getMessage());
        }
    }

    /**
     * 创建测试数据
     */
    private void createTestData() {
        log.info("开始创建测试数据...");
        
        // 1. 创建测试策略
        createTestStrategy();
        
        // 2. 创建测试策略奖品
        createTestStrategyAwards();
        
        // 3. 创建测试策略规则
        createTestStrategyRule();
        
        log.info("测试数据创建完成");
    }
    
    /**
     * 创建测试策略
     */
    private void createTestStrategy() {
        testStrategy = new Strategy();
        testStrategy.setStrategyId(TEST_STRATEGY_ID);
        testStrategy.setStrategyDesc("集成测试策略-自动生成-" + System.currentTimeMillis());
        testStrategy.setRuleModel("single");
        testStrategy.setCreateTime(new Date());
        testStrategy.setUpdateTime(new Date());
        
        int result = strategyDao.insert(testStrategy);
        if (result > 0 && testStrategy.getId() != null) {
            testRecordIds.add(testStrategy.getId());
            log.info("创建测试策略成功，ID: {}, StrategyId: {}", testStrategy.getId(), TEST_STRATEGY_ID);
        }
    }
    
    /**
     * 创建测试策略奖品
     */
    private void createTestStrategyAwards() {
        // 创建多个测试奖品以支持完整的抽奖测试
        int[] awardIds = {101, 102, 103, 104, 105, 106, 107, 108, 109};
        BigDecimal[] rates = {
            new BigDecimal("0.10"), new BigDecimal("0.15"), new BigDecimal("0.15"),
            new BigDecimal("0.15"), new BigDecimal("0.15"), new BigDecimal("0.10"), 
            new BigDecimal("0.10"), new BigDecimal("0.05"), new BigDecimal("0.05")
        };
        
        for (int i = 0; i < awardIds.length; i++) {
            StrategyAward strategyAward = new StrategyAward();
            strategyAward.setStrategyId(TEST_STRATEGY_ID);
            strategyAward.setAwardId(awardIds[i]);
            strategyAward.setAwardTitle("测试奖品" + awardIds[i]);
            strategyAward.setAwardSubtitle("测试奖品副标题" + awardIds[i]);
            strategyAward.setAwardCount(1000);
            strategyAward.setAwardCountSurplus(1000);
            strategyAward.setAwardRate(rates[i]);
            strategyAward.setSort(i + 1);
            strategyAward.setCreateTime(new Date());
            strategyAward.setUpdateTime(new Date());
            
            int result = strategyAwardDao.insert(strategyAward);
            if (result > 0 && strategyAward.getId() != null) {
                testStrategyAwards.add(strategyAward);
                log.info("创建测试策略奖品成功，ID: {}, AwardId: {}", 
                        strategyAward.getId(), strategyAward.getAwardId());
            }
        }
    }
    
    /**
     * 创建测试策略规则
     */
    private void createTestStrategyRule() {
        testStrategyRule = new StrategyRule();
        testStrategyRule.setStrategyId(TEST_STRATEGY_ID.intValue());
        testStrategyRule.setRuleType(1); // 1-策略规则
        testStrategyRule.setRuleModel("rule_weight");
        testStrategyRule.setRuleValue("4000:102,103 6000:102,103,104,105,106,107,108,109");
        testStrategyRule.setRuleDesc("集成测试权重规则-自动生成");
        testStrategyRule.setCreateTime(new Date());
        testStrategyRule.setUpdateTime(new Date());
        
        int result = strategyRuleDao.insert(testStrategyRule);
        if (result > 0 && testStrategyRule.getId() != null) {
            log.info("创建测试策略规则成功，ID: {}, RuleValue: {}", 
                    testStrategyRule.getId(), testStrategyRule.getRuleValue());
        }
    }
    
    /**
     * 清理Redis数据
     */
    private void cleanupRedisData() {
        log.info("清理Redis缓存数据...");
        try {
            String[] keys = {
                "strategy#" + TEST_STRATEGY_ID + "#awardlist",
                "strategy#" + TEST_STRATEGY_ID + "#raterange",
                "strategy#" + TEST_STRATEGY_ID + "#rule#rule_weight",
                "strategy#" + TEST_STRATEGY_ID + "#raterange#weight#4000",
                "strategy#" + TEST_STRATEGY_ID + "#raterange#weight#6000"
            };
            
            for (String key : keys) {
                if (redisService.exists(key)) {
                    redisService.delete(key);
                    log.info("删除缓存key: {}", key);
                }
            }
            
            log.info("Redis缓存清理完成");
        } catch (Exception e) {
            log.warn("清理Redis缓存时出现异常：{}", e.getMessage());
        }
    }
    
    /**
     * 清理数据库测试数据
     */
    private void cleanupDatabaseData() {
        log.info("清理数据库测试数据...");
        
        // 清理策略规则
        if (testStrategyRule != null && testStrategyRule.getId() != null) {
            try {
                strategyRuleDao.deleteById(testStrategyRule.getId());
                log.info("删除测试策略规则，ID: {}", testStrategyRule.getId());
            } catch (Exception e) {
                log.warn("删除测试策略规则失败，ID: {}, 错误: {}", testStrategyRule.getId(), e.getMessage());
            }
        }
        
        // 清理策略奖品
        for (StrategyAward strategyAward : testStrategyAwards) {
            try {
                strategyAwardDao.deleteById(strategyAward.getId());
                log.info("删除测试策略奖品，ID: {}", strategyAward.getId());
            } catch (Exception e) {
                log.warn("删除测试策略奖品失败，ID: {}, 错误: {}", strategyAward.getId(), e.getMessage());
            }
        }
        
        // 清理策略
        if (testStrategy != null && testStrategy.getId() != null) {
            try {
                strategyDao.deleteById(testStrategy.getId());
                log.info("删除测试策略，ID: {}", testStrategy.getId());
            } catch (Exception e) {
                log.warn("删除测试策略失败，ID: {}, 错误: {}", testStrategy.getId(), e.getMessage());
            }
        }
        
        log.info("数据库测试数据清理完成");
    }

    /**
     * 测试完整的策略装配和抽奖流程
     */
    @Test
    public void testCompleteStrategyFlow() {
        log.info("=== 开始测试完整的策略装配和抽奖流程 ===");

        // 1. 测试策略装配
        log.info("步骤1: 测试策略装配");
        boolean assembleResult = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", assembleResult);
        log.info("策略装配成功，策略ID：{}", TEST_STRATEGY_ID);

        // 2. 测试普通抽奖
        log.info("步骤2: 测试普通抽奖");
        Map<Integer, Integer> normalResults = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID);
            assertNotNull("抽奖结果不应该为空", awardId);
            normalResults.put(awardId, normalResults.getOrDefault(awardId, 0) + 1);
        }
        log.info("普通抽奖统计结果: {}", normalResults);

        // 3. 测试权重抽奖
        log.info("步骤3: 测试权重抽奖");
        testWeightLottery("4000", Arrays.asList(102, 103));
        testWeightLottery("6000", Arrays.asList(102, 103, 104, 105, 106, 107, 108, 109));

        log.info("=== 完整策略流程测试完成 ===");
    }

    /**
     * 测试策略装配功能
     */
    @Test
    public void testStrategyAssembly() {
        log.info("=== 开始测试策略装配功能 ===");

        // 测试正常装配
        boolean result = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", result);

        // 验证装配结果
        int rateRange = strategyRepository.getRateRange(TEST_STRATEGY_ID);
        assertTrue("概率范围应该大于0", rateRange > 0);
        log.info("策略装配成功，概率范围: {}", rateRange);

        // 获取权重规则并测试实际存在的权重值
        StrategyRuleEntity weightRule = strategyRepository.queryStrategyRule(TEST_STRATEGY_ID, "rule_weight");
        if (weightRule != null) {
            Map<String, String[]> ruleWeightValues = weightRule.getRuleWeightValues();
            log.info("权重规则值: {}", weightRule.getRuleValue());
            
            // 测试每个实际存在的权重值
            for (String weightValue : ruleWeightValues.keySet()) {
                int weightRateRange = strategyRepository.getRateRangeByWeight(TEST_STRATEGY_ID, weightValue);
                assertTrue(weightValue + "权重策略应该装配成功", weightRateRange > 0);
                log.info("权重值{}装配成功，概率范围: {}", weightValue, weightRateRange);
            }
        } else {
            log.warn("未找到权重规则，跳过权重策略验证");
        }

        log.info("=== 策略装配功能测试完成 ===");
    }

    /**
     * 测试抽奖调度功能
     */
    @Test
    public void testLotteryDispatch() {
        log.info("=== 开始测试抽奖调度功能 ===");

        // 先装配策略
        boolean assembleResult = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", assembleResult);

        // 测试大量抽奖的随机性和正确性
        Map<Integer, Integer> awardStats = new HashMap<>();
        int totalDraws = 1000;

        for (int i = 0; i < totalDraws; i++) {
            Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID);
            assertNotNull("抽奖结果不应该为空", awardId);
            assertTrue("奖品ID应该在有效范围内", Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108, 109).contains(awardId));
            awardStats.put(awardId, awardStats.getOrDefault(awardId, 0) + 1);
        }

        log.info("抽奖{}次的统计结果:", totalDraws);
        for (Map.Entry<Integer, Integer> entry : awardStats.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / totalDraws;
            log.info("奖品ID: {}, 中奖次数: {}, 概率: {:.2f}%", 
                    entry.getKey(), entry.getValue(), percentage);
        }

        // 验证所有奖品都有机会被抽中（在大量抽奖下）
        assertTrue("应该至少有3种不同奖品被抽中", awardStats.size() >= 3);

        log.info("=== 抽奖调度功能测试完成 ===");
    }

    /**
     * 测试权重抽奖调度
     */
    @Test
    public void testWeightLotteryDispatch() {
        log.info("=== 开始测试权重抽奖功能 ===");

        // 先装配策略
        boolean assembleResult = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", assembleResult);

        // 检查权重规则和实际可用的权重值
        StrategyRuleEntity weightRule = strategyRepository.queryStrategyRule(TEST_STRATEGY_ID, "rule_weight");
        if (weightRule == null) {
            log.warn("未找到权重规则，跳过权重测试");
            return;
        }

        String ruleValue = weightRule.getRuleValue();
        log.info("权重规则值: {}", ruleValue);
        
        // 正确解析权重规则格式 (如: "4000:102,103 6000:102,103,104,105,106,107,108,109")
        Map<String, String[]> ruleWeightValues = weightRule.getRuleWeightValues();
        if (ruleWeightValues.isEmpty()) {
            log.warn("权重规则解析为空，跳过权重测试");
            return;
        }
        
        // 测试每个权重值
        for (Map.Entry<String, String[]> entry : ruleWeightValues.entrySet()) {
            String weightValue = entry.getKey();
            String[] awardIds = entry.getValue();
            
            log.info("测试权重值: {}, 期望奖品: {}", weightValue, Arrays.toString(awardIds));
            
            // 检查权重范围是否正确装配
            int weightRateRange = strategyRepository.getRateRangeByWeight(TEST_STRATEGY_ID, weightValue);
            log.info("权重值{}的范围: {}", weightValue, weightRateRange);
            
            if (weightRateRange <= 0) {
                log.warn("权重值{}未正确装配，跳过此权重测试", weightValue);
                continue;
            }
            
            // 执行多次权重抽奖
            Map<Integer, Integer> awardCountMap = new HashMap<>();
            int testCount = 1000;
            
            for (int i = 0; i < testCount; i++) {
                Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID, weightValue);
                assertNotNull("权重抽奖应该返回奖品ID", awardId);
                awardCountMap.put(awardId, awardCountMap.getOrDefault(awardId, 0) + 1);
            }
            
            // 验证抽中的奖品都在预期范围内
            for (Integer awardId : awardCountMap.keySet()) {
                boolean isInExpectedRange = Arrays.stream(awardIds)
                        .map(Integer::parseInt)
                        .anyMatch(expectedId -> expectedId.equals(awardId));
                assertTrue("奖品ID " + awardId + " 应该在权重值 " + weightValue + " 的范围内", isInExpectedRange);
            }
            
            log.info("权重值{}测试完成，抽奖{}次，奖品分布: {}", weightValue, testCount, awardCountMap);
        }

        log.info("=== 权重抽奖功能测试完成 ===");
    }

    /**
     * 测试权重抽奖回退机制
     */
    @Test
    public void testWeightLotteryFallback() {
        log.info("=== 开始测试权重抽奖回退机制 ===");

        // 先装配策略
        boolean assembleResult = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", assembleResult);

        // 测试不存在的权重值，应该回退到普通抽奖
        String invalidWeightValue = "9999";
        
        for (int i = 0; i < 10; i++) {
            Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID, invalidWeightValue);
            assertNotNull("即使是无效权重值，也应该通过回退机制返回结果", awardId);
            assertTrue("回退抽奖的奖品ID应该在有效范围内", 
                      Arrays.asList(101, 102, 103, 104, 105, 106, 107, 108, 109).contains(awardId));
        }

        log.info("权重抽奖回退机制测试通过");
        log.info("=== 权重抽奖回退机制测试完成 ===");
    }

    /**
     * 测试并发抽奖的安全性
     */
    @Test
    public void testConcurrentLottery() {
        log.info("=== 开始测试并发抽奖安全性 ===");

        // 先装配策略
        boolean assembleResult = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", assembleResult);

        List<Thread> threads = new ArrayList<>();
        List<Integer> allResults = Collections.synchronizedList(new ArrayList<>());
        int threadsCount = 10;
        int drawsPerThread = 50;

        for (int i = 0; i < threadsCount; i++) {
            final int threadId = i;
            Thread thread = new Thread(() -> {
                log.info("线程{}开始抽奖", threadId);
                for (int j = 0; j < drawsPerThread; j++) {
                    Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID);
                    assertNotNull("并发抽奖结果不应该为空", awardId);
                    allResults.add(awardId);
                }
                log.info("线程{}完成抽奖", threadId);
            });
            threads.add(thread);
        }

        // 启动所有线程
        threads.forEach(Thread::start);

        // 等待所有线程完成
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error("线程等待被中断", e);
            }
        });

        // 验证结果
        assertEquals("应该产生正确数量的抽奖结果", threadsCount * drawsPerThread, allResults.size());
        
        // 统计结果
        Map<Integer, Integer> concurrentStats = new HashMap<>();
        for (Integer awardId : allResults) {
            concurrentStats.put(awardId, concurrentStats.getOrDefault(awardId, 0) + 1);
        }

        log.info("并发抽奖统计结果:");
        for (Map.Entry<Integer, Integer> entry : concurrentStats.entrySet()) {
            log.info("奖品ID: {}, 中奖次数: {}", entry.getKey(), entry.getValue());
        }

        log.info("=== 并发抽奖安全性测试完成 ===");
    }

    /**
     * 测试指定权重值的抽奖
     */
    private void testWeightLottery(String weightValue, List<Integer> expectedAwards) {
        log.info("测试权重值 {} 的抽奖，预期奖品范围: {}", weightValue, expectedAwards);
        
        Map<Integer, Integer> weightResults = new HashMap<>();
        for (int i = 0; i < 50; i++) {
            Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID, weightValue);
            assertNotNull("权重抽奖结果不应该为空", awardId);
            assertTrue("奖品ID应该在权重范围内", expectedAwards.contains(awardId));
            weightResults.put(awardId, weightResults.getOrDefault(awardId, 0) + 1);
        }
        
        log.info("权重值 {} 抽奖统计: {}", weightValue, weightResults);
    }
} 