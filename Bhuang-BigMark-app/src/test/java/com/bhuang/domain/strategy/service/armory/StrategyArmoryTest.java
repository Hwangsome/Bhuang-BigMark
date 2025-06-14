package com.bhuang.domain.strategy.service.armory;

import com.bhuang.domain.strategy.model.entity.StrategyAwardEntity;
import com.bhuang.domain.strategy.repository.IStrategyRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

/**
 * 策略装配库Mock测试类
 * 使用Mock测试策略装配和随机抽奖的行为逻辑
 * @author bhuang
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class StrategyArmoryTest {

    @Mock
    private IStrategyRepository strategyRepository;

    @InjectMocks
    private StrategyArmory strategyArmory;

    /**
     * 测试用的策略ID
     */
    private static final Long TEST_STRATEGY_ID = 100001L;
    
    /**
     * 模拟的策略奖品列表
     */
    private List<StrategyAwardEntity> mockStrategyAwardList;

    @Before
    public void setUp() {
        log.info("=== 策略装配库Mock测试初始化 ===");
        
        // 准备模拟数据
        mockStrategyAwardList = createMockStrategyAwardList();
    }

    @Test
    public void testAssembleLotteryStrategy() {
        log.info("=== 测试装配抽奖策略 ===");
        
        // 模拟策略仓储返回奖品列表
        when(strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID))
                .thenReturn(mockStrategyAwardList);
        
        // 执行策略装配
        boolean result = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        
        // 验证装配结果
        assertTrue("策略装配应该成功", result);
        
        // 验证调用行为
        verify(strategyRepository, times(1)).queryStrategyAwardList(TEST_STRATEGY_ID);
        verify(strategyRepository, times(1)).storeStrategyAwardSearchRateTable(
                eq(TEST_STRATEGY_ID), anyInt(), anyMap());
        
        log.info("✓ 策略装配成功，验证了正确的调用行为");
    }

    @Test
    public void testAssembleLotteryStrategyWithEmptyList() {
        log.info("=== 测试装配空策略列表 ===");
        
        // 模拟策略仓储返回空列表
        when(strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID))
                .thenReturn(Collections.emptyList());
        
        // 执行策略装配
        boolean result = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        
        // 验证装配结果
        assertFalse("空策略列表装配应该失败", result);
        
        // 验证调用行为
        verify(strategyRepository, times(1)).queryStrategyAwardList(TEST_STRATEGY_ID);
        verify(strategyRepository, never()).storeStrategyAwardSearchRateTable(anyLong(), anyInt(), anyMap());
        
        log.info("✓ 空策略列表处理正确");
    }

    @Test
    public void testAssembleLotteryStrategyWithNullList() {
        log.info("=== 测试装配null策略列表 ===");
        
        // 模拟策略仓储返回null
        when(strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID))
                .thenReturn(null);
        
        // 执行策略装配
        boolean result = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        
        // 验证装配结果
        assertFalse("null策略列表装配应该失败", result);
        
        // 验证调用行为
        verify(strategyRepository, times(1)).queryStrategyAwardList(TEST_STRATEGY_ID);
        verify(strategyRepository, never()).storeStrategyAwardSearchRateTable(anyLong(), anyInt(), anyMap());
        
        log.info("✓ null策略列表处理正确");
    }

    @Test
    public void testGetRandomAwardId() {
        log.info("=== 测试随机获取奖品ID ===");
        
        // 模拟策略已装配，概率范围为1000
        int mockRateRange = 1000;
        when(strategyRepository.getRateRange(TEST_STRATEGY_ID))
                .thenReturn(mockRateRange);
        
        // 模拟概率查找表返回奖品ID
        Integer expectedAwardId = 101;
        when(strategyRepository.getStrategyAwardAssemble(eq(TEST_STRATEGY_ID), anyInt()))
                .thenReturn(expectedAwardId);
        
        // 执行随机抽奖
        Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID);
        
        // 验证结果
        assertNotNull("随机抽奖结果不应该为null", awardId);
        assertEquals("应该返回期望的奖品ID", expectedAwardId, awardId);
        
        // 验证调用行为
        verify(strategyRepository, times(1)).getRateRange(TEST_STRATEGY_ID);
        verify(strategyRepository, times(1)).getStrategyAwardAssemble(eq(TEST_STRATEGY_ID), anyInt());
        
        log.info("✓ 随机抽奖行为验证通过，奖品ID：{}", awardId);
    }

    @Test
    public void testGetRandomAwardIdWithUnassembledStrategy() {
        log.info("=== 测试未装配策略的随机抽奖 ===");
        
        Long unassembledStrategyId = 999999L;
        
        // 模拟未装配策略，概率范围为0
        when(strategyRepository.getRateRange(unassembledStrategyId))
                .thenReturn(0);
        
        // 执行随机抽奖
        Integer awardId = strategyArmory.getRandomAwardId(unassembledStrategyId);
        
        // 验证结果
        assertNull("未装配的策略应该返回null", awardId);
        
        // 验证调用行为
        verify(strategyRepository, times(1)).getRateRange(unassembledStrategyId);
        verify(strategyRepository, never()).getStrategyAwardAssemble(anyLong(), anyInt());
        
        log.info("✓ 未装配策略处理正确");
    }

    @Test
    public void testGetRandomAwardIdWithWeightRule() {
        log.info("=== 测试带权重规则的随机抽奖 ===");
        
        String ruleWeightValue = "6000,102,103,104";
        
        // 模拟策略已装配
        when(strategyRepository.getRateRange(TEST_STRATEGY_ID))
                .thenReturn(1000);
        when(strategyRepository.getStrategyAwardAssemble(eq(TEST_STRATEGY_ID), anyInt()))
                .thenReturn(102);
        
        // 执行带权重规则的随机抽奖
        Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID, ruleWeightValue);
        
        // 验证结果（当前实现是调用普通随机逻辑）
        assertNotNull("带权重规则的随机抽奖结果不应该为null", awardId);
        
        // 验证调用行为
        verify(strategyRepository, times(1)).getRateRange(TEST_STRATEGY_ID);
        verify(strategyRepository, times(1)).getStrategyAwardAssemble(eq(TEST_STRATEGY_ID), anyInt());
        
        log.info("✓ 带权重规则的随机抽奖行为验证通过");
    }

    @Test
    public void testAssembleLotteryStrategyByActivityId() {
        log.info("=== 测试根据活动ID装配策略 ===");
        
        Long activityId = 10001L;
        
        // 执行根据活动ID装配策略（当前实现返回false）
        Boolean result = strategyArmory.assembleLotteryStrategyByActivityId(activityId);
        
        // 验证结果
        assertFalse("当前实现应该返回false", result);
        
        log.info("✓ 根据活动ID装配策略行为验证通过");
    }

    @Test
    public void testRepositoryInteractionFlow() {
        log.info("=== 测试仓储交互流程 ===");
        
        // 模拟完整的装配流程
        when(strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID))
                .thenReturn(mockStrategyAwardList);
        
        // 执行装配
        boolean assembleResult = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("装配应该成功", assembleResult);
        
        // 模拟装配后的查询
        when(strategyRepository.getRateRange(TEST_STRATEGY_ID))
                .thenReturn(166); // 基于新公式计算的概率范围
        when(strategyRepository.getStrategyAwardAssemble(eq(TEST_STRATEGY_ID), anyInt()))
                .thenReturn(101);
        
        // 执行随机抽奖
        Integer awardId = strategyArmory.getRandomAwardId(TEST_STRATEGY_ID);
        assertNotNull("抽奖结果不应该为null", awardId);
        
        // 验证完整的调用流程
        verify(strategyRepository, times(1)).queryStrategyAwardList(TEST_STRATEGY_ID);
        verify(strategyRepository, times(1)).storeStrategyAwardSearchRateTable(
                eq(TEST_STRATEGY_ID), anyInt(), anyMap());
        verify(strategyRepository, times(1)).getRateRange(TEST_STRATEGY_ID);
        verify(strategyRepository, times(1)).getStrategyAwardAssemble(eq(TEST_STRATEGY_ID), anyInt());
        
        log.info("✓ 仓储交互流程验证通过");
    }
    
    @Test
    public void testCalculateRateRangeWithNewFormula() {
        log.info("=== 测试新公式计算概率范围 ===");
        
        // 准备测试数据
        List<StrategyAwardEntity> testAwardList = new ArrayList<>();
        testAwardList.add(createMockAward(101, "一等奖", new BigDecimal("1"))); // 1%
        testAwardList.add(createMockAward(102, "二等奖", new BigDecimal("4"))); // 4%
        testAwardList.add(createMockAward(103, "三等奖", new BigDecimal("15"))); // 15%
        testAwardList.add(createMockAward(104, "安慰奖", new BigDecimal("80"))); // 80%
        
        // 模拟策略仓储返回奖品列表
        when(strategyRepository.queryStrategyAwardList(TEST_STRATEGY_ID))
                .thenReturn(testAwardList);
        
        // 在装配过程中会调用calculateRateRange方法
        boolean result = strategyArmory.assembleLotteryStrategy(TEST_STRATEGY_ID);
        assertTrue("策略装配应该成功", result);
        
        // 验证调用行为，捕获传递的概率范围参数
        ArgumentCaptor<Integer> rateRangeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(strategyRepository).storeStrategyAwardSearchRateTable(
                eq(TEST_STRATEGY_ID), 
                rateRangeCaptor.capture(), 
                anyMap());
        
        // 获取捕获的概率范围参数
        Integer capturedRateRange = rateRangeCaptor.getValue();
        
        // 预期的概率范围：总概率100% / 最小概率1% = 100
        int expectedRateRange = 100;
        
        // 验证计算结果
        // 100% / 1% = 100，预期范围是100
        assertEquals("使用新公式计算的概率范围应该是100", expectedRateRange, capturedRateRange.intValue());
        
        log.info("✓ 新公式计算概率范围验证通过，总概率100% / 最小概率1% = {}", capturedRateRange);
    }

    /**
     * 创建模拟的策略奖品列表
     * @return 策略奖品列表
     */
    private List<StrategyAwardEntity> createMockStrategyAwardList() {
        List<StrategyAwardEntity> list = new ArrayList<>();

        list.add(createMockAward(101, "随机积分", new BigDecimal("0.6")));
        list.add(createMockAward(102, "5积分", new BigDecimal("10")));
        list.add(createMockAward(103, "10积分", new BigDecimal("20")));
        list.add(createMockAward(104, "20积分", new BigDecimal("30")));
        list.add(createMockAward(105, "50积分", new BigDecimal("38.4")));
        
        return list;
    }

    /**
     * 创建模拟奖品实体
     * @param awardId 奖品ID
     * @param title 奖品标题
     * @param rate 中奖概率
     * @return 奖品实体
     */
    private StrategyAwardEntity createMockAward(Integer awardId, String title, BigDecimal rate) {
        StrategyAwardEntity award = new StrategyAwardEntity();
        award.setStrategyId(TEST_STRATEGY_ID);
        award.setAwardId(awardId);
        award.setAwardTitle(title);
        award.setAwardRate(rate);
        award.setAwardCount(1000);
        award.setAwardCountSurplus(1000);
        award.setSort(awardId);
        return award;
    }
} 